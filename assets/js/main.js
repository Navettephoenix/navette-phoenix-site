const WEBHOOK_URL = "";   // vide par défaut
const WEBHOOK_TOKEN = ""; // vide par défaut

const PHONE_REGEX = /^(?:\+33|0)\s?(?:[1-5]|7|6|9)(?:[\s\.-]?\d{2}){4}$/;
const EMAIL_REGEX = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

function getToastElement() {
  let toast = document.querySelector('.toast');
  if (!toast) {
    toast = document.createElement('div');
    toast.className = 'toast';
    toast.setAttribute('role', 'status');
    toast.setAttribute('aria-live', 'polite');
    document.body.appendChild(toast);
  }
  return toast;
}

function showToast(message, isError = false) {
  const toast = getToastElement();
  toast.textContent = message;
  toast.classList.toggle('error', isError);
  toast.classList.add('show');
  setTimeout(() => {
    toast.classList.remove('show');
  }, 5000);
}

function clearErrors(form) {
  form.querySelectorAll('.error-message').forEach((msg) => {
    msg.textContent = '';
  });
  form.querySelectorAll('[aria-invalid="true"]').forEach((input) => {
    input.setAttribute('aria-invalid', 'false');
  });
}

function showFieldError(input, message) {
  const errorId = `${input.id}-error`;
  const errorElement = document.getElementById(errorId);
  if (errorElement) {
    errorElement.textContent = message;
  }
  input.setAttribute('aria-invalid', 'true');
}

function validateForm(form) {
  clearErrors(form);
  let isValid = true;
  let firstInvalid = null;

  const inputs = Array.from(form.querySelectorAll('input, select, textarea'));
  inputs.forEach((input) => {
    if (input.disabled) {
      return;
    }

    let valid = input.checkValidity();

    if (valid && input.type === 'tel') {
      valid = PHONE_REGEX.test(input.value.trim());
      if (!valid) {
        showFieldError(input, 'Merci de saisir un numéro français valide.');
      }
    }

    if (valid && input.type === 'email') {
      valid = EMAIL_REGEX.test(input.value.trim());
      if (!valid) {
        showFieldError(input, 'Merci de saisir un email valide.');
      }
    }

    if (!valid) {
      if (!input.validationMessage && input.required && !input.value.trim()) {
        showFieldError(input, 'Ce champ est requis.');
      } else if (!input.validationMessage) {
        showFieldError(input, 'Valeur invalide.');
      } else {
        showFieldError(input, input.validationMessage);
      }
      isValid = false;
      if (!firstInvalid) {
        firstInvalid = input;
      }
    }
  });

  if (firstInvalid) {
    firstInvalid.focus();
  }

  return isValid;
}

function buildMessage(formData) {
  const entries = [];
  for (const [key, value] of formData.entries()) {
    entries.push(`${key} : ${value}`);
  }
  return entries.join('\n');
}

function disableSubmit(form, disabled) {
  const submit = form.querySelector('button[type="submit"]');
  if (!submit) {
    return;
  }

  if (disabled) {
    if (!submit.dataset.originalText) {
      submit.dataset.originalText = submit.textContent.trim();
    }
    submit.textContent = 'Envoi…';
    submit.setAttribute('aria-busy', 'true');
    submit.classList.add('is-loading');
  } else {
    if (submit.dataset.originalText) {
      submit.textContent = submit.dataset.originalText;
      delete submit.dataset.originalText;
    }
    submit.removeAttribute('aria-busy');
    submit.classList.remove('is-loading');
  }

  submit.disabled = disabled;
}

function handleMapButtons() {
  document.querySelectorAll('[data-map-trigger]').forEach((button) => {
    button.addEventListener('click', () => {
      const form = button.closest('form');
      if (!form) return;
      const targetName = button.getAttribute('data-map-trigger');
      const field = form.querySelector(`[name="${targetName}"]`);
      if (field && field.value.trim()) {
        const url = `https://www.google.com/maps/search/?api=1&query=${encodeURIComponent(field.value.trim())}`;
        window.open(url, '_blank', 'noopener');
      } else {
        showToast('Renseignez une destination pour ouvrir la carte.', true);
        if (field) {
          field.focus();
        }
      }
    });
  });
}

function sendViaWebhook(formType, payload) {
  const headers = {
    'Content-Type': 'application/x-www-form-urlencoded'
  };
  if (WEBHOOK_TOKEN) {
    headers.Authorization = `Bearer ${WEBHOOK_TOKEN}`;
  }
  const body = new URLSearchParams(payload).toString();
  return fetch(WEBHOOK_URL, {
    method: 'POST',
    headers,
    body
  });
}

function sendViaMailto(subject, body, replyTo) {
  const params = new URLSearchParams({
    subject,
    body
  });
  if (replyTo) {
    params.append('reply-to', replyTo);
  }
  const mailto = `mailto:caro.resanavettephoenix@gmail.com?cc=phoenix.lts28@gmail.com&${params.toString()}`;
  window.location.href = mailto;
  return Promise.resolve();
}

function handleSubmit(event) {
  event.preventDefault();
  const form = event.currentTarget;

  if (!validateForm(form)) {
    return;
  }

  const formData = new FormData(form);
  const formType = form.getAttribute('data-form-type');
  const isReservation = formType === 'reservation_tad';
  const subject = isReservation ? 'TAD – Réservation (5 €)' : 'Hors Secteur – Devis';
  const body = buildMessage(formData);

  const payload = {
    type: formType,
    subject,
    to: 'caro.resanavettephoenix@gmail.com',
    cc: 'phoenix.lts28@gmail.com',
    message: body
  };

  if (!isReservation) {
    const email = formData.get('email');
    if (email) {
      payload.replyTo = email;
    }
  }

  const useWebhook = Boolean(WEBHOOK_URL);
  disableSubmit(form, true);

  if (!useWebhook) {
    try {
      sendViaMailto(subject, body, payload.replyTo);
      showToast('Votre email est prêt à être envoyé dans votre messagerie.', false);
      form.reset();
    } catch (error) {
      showToast('Impossible d\'ouvrir votre messagerie. Merci de réessayer ou de nous contacter par téléphone.', true);
    } finally {
      // Réactive immédiatement le bouton pour éviter un blocage si l'ouverture mailto est annulée.
      disableSubmit(form, false);
    }
    return;
  }

  sendViaWebhook(formType, payload)
    .then((response) => {
      if (!response.ok) {
        throw new Error('Réponse serveur invalide');
      }
      showToast('Votre demande a bien été envoyée.', false);
      form.reset();
    })
    .catch(() => {
      showToast('Une erreur est survenue. Merci de réessayer ou de nous contacter par téléphone.', true);
    })
    .finally(() => {
      disableSubmit(form, false);
    });
}

function initForms() {
  document.querySelectorAll('form[data-form-type]').forEach((form) => {
    form.setAttribute('noValidate', 'true');
    form.addEventListener('submit', handleSubmit);
  });
}

document.addEventListener('DOMContentLoaded', () => {
  initForms();
  handleMapButtons();
});
