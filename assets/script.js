const appsScriptUrl = 'https://script.google.com/macros/s/AKfycbzH67AeoCSPMdGa_apXqT3juZVGVWzdcVv5Qp6iZ27aWMk-PFgadloIum5TjBtFxBFE/exec';

const toast = document.querySelector('.toast');
const currentYearEl = document.getElementById('current-year');
const successModal = document.getElementById('success-modal');
const successMessageEl = document.getElementById('success-message');
const modalBackdrop = document.querySelector('.modal-backdrop');
const modalCloseButtons = document.querySelectorAll('[data-close-modal]');

if (currentYearEl) {
  currentYearEl.textContent = new Date().getFullYear();
}

const showToast = (message, isError = false) => {
  if (!toast) return;
  toast.textContent = message;
  toast.classList.toggle('error', Boolean(isError));
  toast.hidden = false;
  setTimeout(() => {
    toast.hidden = true;
    toast.classList.remove('error');
  }, 5000);
};

const openSuccessModal = (message) => {
  if (!successModal || !modalBackdrop || !successMessageEl) {
    showToast(message);
    return;
  }
  successMessageEl.innerHTML = message;
  modalBackdrop.hidden = false;
  successModal.hidden = false;
  successModal.focus({ preventScroll: true });
};

const closeSuccessModal = () => {
  if (!successModal || !modalBackdrop) return;
  modalBackdrop.hidden = true;
  successModal.hidden = true;
};

modalCloseButtons.forEach((button) => {
  button.addEventListener('click', () => closeSuccessModal());
});

if (modalBackdrop) {
  modalBackdrop.addEventListener('click', () => closeSuccessModal());
}

document.addEventListener('keydown', (event) => {
  if (event.key === 'Escape') {
    closeSuccessModal();
  }
});

const formatPhone = (value) => value.replace(/[^+\d]/g, '');

const validateRequired = (input) => {
  const id = input.id || input.name;
  const errorEl = document.querySelector(`[data-error-for="${id}"]`);
  let message = '';

  if (input.type === 'radio') {
    const checked = document.querySelector(`input[name="${input.name}"]:checked`);
    if (!checked) {
      message = 'Veuillez sélectionner une option.';
    }
  } else if (input.tagName === 'SELECT' && !input.value) {
    message = 'Veuillez sélectionner une option.';
  } else if (!input.value.trim()) {
    message = 'Ce champ est obligatoire.';
  } else if (input.type === 'tel' && input.pattern) {
    const regex = new RegExp(input.pattern);
    if (!regex.test(formatPhone(input.value))) {
      message = 'Veuillez saisir un numéro de téléphone français valide.';
    }
  } else if (input.type === 'email') {
    const emailRegex = /.+@.+\..+/;
    if (!emailRegex.test(input.value.trim())) {
      message = 'Veuillez saisir une adresse e-mail valide.';
    }
  }

  if (errorEl) {
    errorEl.textContent = message;
  }

  if (input.type !== 'radio') {
    input.setAttribute('aria-invalid', message ? 'true' : 'false');
  }

  return message === '';
};

const attachValidation = (form) => {
  const fields = form.querySelectorAll('input[required], select[required]');
  fields.forEach((field) => {
    const eventName = field.type === 'radio' ? 'change' : 'input';
    field.addEventListener(eventName, () => validateRequired(field));
    field.addEventListener('blur', () => validateRequired(field));
  });
};

const serializeForm = (form) => {
  const data = new FormData(form);
  const payload = {};
  data.forEach((value, key) => {
    if (payload[key]) {
      if (Array.isArray(payload[key])) {
        payload[key].push(value);
      } else {
        payload[key] = [payload[key], value];
      }
    } else {
      payload[key] = value;
    }
  });
  payload.form_name = form.id;
  return payload;
};

const startButtonLoading = (button) => {
  if (!button) return;
  const loadingText = button.dataset.loadingText || 'Chargement…';
  if (!button.dataset.originalLabel) {
    const label = button.querySelector('.btn-label');
    button.dataset.originalLabel = label ? label.textContent : button.textContent;
  }
  const label = button.querySelector('.btn-label');
  if (label) {
    label.textContent = loadingText;
  } else {
    button.textContent = loadingText;
  }
  button.disabled = true;
  button.classList.add('is-loading');
  button.setAttribute('aria-busy', 'true');
};

const stopButtonLoading = (button) => {
  if (!button) return;
  const original = button.dataset.originalLabel;
  const label = button.querySelector('.btn-label');
  if (original) {
    if (label) {
      label.textContent = original;
    } else {
      button.textContent = original;
    }
  }
  button.disabled = false;
  button.classList.remove('is-loading');
  button.removeAttribute('aria-busy');
  delete button.dataset.originalLabel;
};

const DEFAULT_SUCCESS_MESSAGE = '✔ Un conseiller vous rappelle sous 2&nbsp;h (9h–19h, lun–sam).';

const handleSubmit = (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const submitButton = form.querySelector('button[type="submit"]');
  const requiredFields = form.querySelectorAll('input[required], select[required]');
  let formIsValid = true;

  requiredFields.forEach((field) => {
    if (field.type === 'radio') {
      const name = field.name;
      const radios = form.querySelectorAll(`input[name="${name}"]`);
      const checked = Array.from(radios).some((radio) => radio.checked);
      const errorEl = form.querySelector(`[data-error-for="${name}"]`);
      if (!checked) {
        formIsValid = false;
        if (errorEl) errorEl.textContent = 'Veuillez sélectionner une option.';
      } else if (errorEl) {
        errorEl.textContent = '';
      }
    } else if (!validateRequired(field)) {
      if (formIsValid) {
        field.focus();
      }
      formIsValid = false;
    }
  });

  if (!formIsValid) {
    showToast('Merci de vérifier les champs en rouge.', true);
    return;
  }

  startButtonLoading(submitButton);

  const payload = serializeForm(form);

  if (form.id === 'tad-form' || form.id === 'devis-form') {
    payload.recipient_email = 'caro.resanavettephoenix@gmail.com';
    payload.manager_email = 'Phoenix.lts28@navettephoenix.fr';
  }

  fetch(appsScriptUrl, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(payload),
  })
    .then((response) => {
      if (!response.ok && (response.status < 200 || response.status >= 400)) {
        throw new Error(`Erreur réseau (${response.status})`);
      }
      return response.json().catch(() => ({}));
    })
    .then(() => {
      form.reset();
      const totalEl = form.querySelector('#tad-total');
      if (totalEl) {
        totalEl.textContent = '0 €';
      }
      openSuccessModal(DEFAULT_SUCCESS_MESSAGE);
    })
    .catch((error) => {
      console.error(error);
      showToast('Une erreur est survenue. Merci de réessayer dans quelques instants.', true);
    })
    .finally(() => {
      stopButtonLoading(submitButton);
    });
};

const tadForm = document.getElementById('tad-form');
const devisForm = document.getElementById('devis-form');

if (tadForm) {
  attachValidation(tadForm);
  tadForm.addEventListener('submit', handleSubmit);
  const passengersSelect = document.getElementById('tad-passengers');
  const totalEl = document.getElementById('tad-total');
  const pricePerPassenger = 5;
  if (passengersSelect && totalEl) {
    const updateTotal = () => {
      const passengers = Number(passengersSelect.value);
      if (passengers > 0) {
        const total = passengers * pricePerPassenger;
        totalEl.textContent = `${total.toLocaleString('fr-FR')} €`;
      } else {
        totalEl.textContent = '0 €';
      }
    };
    passengersSelect.addEventListener('change', updateTotal);
    updateTotal();
  }
}

if (devisForm) {
  attachValidation(devisForm);
  devisForm.addEventListener('submit', handleSubmit);
}

const dateTimeInputs = document.querySelectorAll('input[type="date"], input[type="time"]');
dateTimeInputs.forEach((input) => {
  input.addEventListener('keydown', (event) => {
    const allowedKeys = ['Tab', 'Shift', 'ArrowLeft', 'ArrowRight', 'ArrowUp', 'ArrowDown', 'Enter', 'Escape'];
    if (!allowedKeys.includes(event.key)) {
      event.preventDefault();
    }
  });
  input.addEventListener('focus', () => {
    if (typeof input.showPicker === 'function') {
      input.showPicker();
    }
  });
});

// Comparateur de trajets
const compareForm = document.getElementById('compare-form');
const compareMapElement = document.getElementById('compare-map');
const distanceEl = document.getElementById('result-distance');
const shuttleEl = document.getElementById('result-shuttle');
const carEl = document.getElementById('result-car');
const useLocationButton = document.getElementById('compare-use-location');

let compareMap;
let routeLayer;
let pickupMarker;
let dropoffMarker;

const initMap = () => {
  if (!compareMapElement || typeof L === 'undefined') return;
  compareMap = L.map(compareMapElement, {
    center: [48.32, 0.82],
    zoom: 9,
    zoomControl: false,
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors',
  }).addTo(compareMap);

  L.control.zoom({ position: 'bottomright' }).addTo(compareMap);
};

const geocodeAddress = async (query) => {
  const url = new URL('https://nominatim.openstreetmap.org/search');
  url.searchParams.set('format', 'json');
  url.searchParams.set('limit', '1');
  url.searchParams.set('addressdetails', '0');
  url.searchParams.set('countrycodes', 'fr');
  url.searchParams.set('q', `${query}, France`);

  const response = await fetch(url.toString(), {
    headers: {
      'Accept-Language': 'fr',
    },
  });

  if (!response.ok) {
    throw new Error('Impossible de géocoder cette adresse.');
  }

  const results = await response.json();
  if (!Array.isArray(results) || results.length === 0) {
    throw new Error('Adresse introuvable. Merci de préciser la commune.');
  }

  const [result] = results;
  return {
    lat: Number(result.lat),
    lon: Number(result.lon),
    label: result.display_name,
  };
};

const reverseGeocode = async (lat, lon) => {
  const url = new URL('https://nominatim.openstreetmap.org/reverse');
  url.searchParams.set('format', 'json');
  url.searchParams.set('lat', String(lat));
  url.searchParams.set('lon', String(lon));
  url.searchParams.set('zoom', '16');
  url.searchParams.set('addressdetails', '0');

  const response = await fetch(url.toString(), {
    headers: {
      'Accept-Language': 'fr',
    },
  });

  if (!response.ok) {
    throw new Error('Impossible de déterminer votre adresse.');
  }

  const data = await response.json();
  if (!data || !data.display_name) {
    throw new Error('Adresse introuvable pour cette position.');
  }

  return {
    lat,
    lon,
    label: data.display_name,
  };
};

const fetchRoute = async (start, end) => {
  const url = `https://router.project-osrm.org/route/v1/driving/${start.lon},${start.lat};${end.lon},${end.lat}?overview=full&geometries=geojson`;
  const response = await fetch(url);
  if (!response.ok) {
    throw new Error('Impossible de calculer l’itinéraire.');
  }
  const data = await response.json();
  if (!data.routes || !data.routes.length) {
    throw new Error('Aucun itinéraire trouvé.');
  }
  return data.routes[0];
};

const formatDistance = (meters) => {
  const km = meters / 1000;
  return `${km.toLocaleString('fr-FR', { maximumFractionDigits: 1 })} km`;
};

const formatDuration = (minutes) => {
  const rounded = Math.round(minutes);
  const hours = Math.floor(rounded / 60);
  const mins = rounded % 60;
  if (hours > 0) {
    return `${hours} h ${mins.toString().padStart(2, '0')} min`;
  }
  return `${mins} min`;
};

const updateMap = (route, start, end) => {
  if (!compareMap) return;

  if (routeLayer) {
    routeLayer.remove();
  }
  if (pickupMarker) {
    pickupMarker.remove();
  }
  if (dropoffMarker) {
    dropoffMarker.remove();
  }

  routeLayer = L.geoJSON(route.geometry, {
    style: {
      color: '#ff8c24',
      weight: 5,
      opacity: 0.85,
    },
  }).addTo(compareMap);

  pickupMarker = L.marker([start.lat, start.lon], {
    title: 'Prise en charge',
  }).addTo(compareMap);

  dropoffMarker = L.marker([end.lat, end.lon], {
    title: 'Dépose',
  }).addTo(compareMap);

  const bounds = routeLayer.getBounds();
  if (bounds.isValid()) {
    compareMap.fitBounds(bounds, { padding: [32, 32] });
  }
};

const handleCompareSubmit = async (event) => {
  event.preventDefault();
  if (!compareForm) return;

  const submitButton = compareForm.querySelector('button[type="submit"]');
  const requiredFields = compareForm.querySelectorAll('input[required]');
  let formIsValid = true;

  requiredFields.forEach((field) => {
    if (!validateRequired(field)) {
      if (formIsValid) {
        field.focus();
      }
      formIsValid = false;
    }
  });

  if (!formIsValid) {
    showToast('Merci de vérifier les champs en rouge.', true);
    return;
  }

  startButtonLoading(submitButton);

  const pickupField = compareForm.elements.namedItem('compare_pickup');
  const dropoffField = compareForm.elements.namedItem('compare_dropoff');

  if (!pickupField || !dropoffField) {
    stopButtonLoading(submitButton);
    showToast('Champs de formulaire introuvables.', true);
    return;
  }

  try {
    const [pickup, dropoff] = await Promise.all([
      geocodeAddress(pickupField.value),
      geocodeAddress(dropoffField.value),
    ]);

    const route = await fetchRoute(pickup, dropoff);

    const carDurationMinutes = route.duration / 60;
    const shuttleDurationMinutes = carDurationMinutes + 10; // marge opérationnelle

    if (distanceEl) {
      distanceEl.textContent = formatDistance(route.distance);
    }
    if (carEl) {
      carEl.textContent = formatDuration(carDurationMinutes);
    }
    if (shuttleEl) {
      shuttleEl.textContent = formatDuration(shuttleDurationMinutes);
    }

    updateMap(route, pickup, dropoff);
  } catch (error) {
    console.error(error);
    showToast(error.message || 'Impossible de calculer ce trajet pour le moment.', true);
  } finally {
    stopButtonLoading(submitButton);
  }
};

if (compareMapElement) {
  initMap();
}

if (compareForm) {
  attachValidation(compareForm);
  compareForm.addEventListener('submit', handleCompareSubmit);
}

if (useLocationButton) {
  if (!('geolocation' in navigator)) {
    useLocationButton.hidden = true;
  } else {
    useLocationButton.addEventListener('click', () => {
      startButtonLoading(useLocationButton);
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          try {
            const { latitude, longitude } = position.coords;
            const result = await reverseGeocode(latitude, longitude);
            const pickupInput = document.getElementById('compare-pickup');
            if (pickupInput) {
              pickupInput.value = result.label;
              pickupInput.focus();
            }
            showToast('Position détectée, vérifiez l’adresse avant envoi.');
          } catch (error) {
            console.error(error);
            showToast('Impossible de récupérer votre adresse actuelle.', true);
          } finally {
            stopButtonLoading(useLocationButton);
          }
        },
        (error) => {
          console.error(error);
          showToast("Accès à la localisation refusé.", true);
          stopButtonLoading(useLocationButton);
        },
        {
          enableHighAccuracy: true,
          timeout: 10000,
        }
      );
    });
  }
}
