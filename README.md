# Navette Phoenix – Site statique

Ce dépôt contient le site statique de Navette Phoenix, pensé pour un déploiement simple sur GitHub Pages.

## Prérequis
- Un compte GitHub avec les droits d'édition sur ce dépôt.

## Déploiement sur GitHub Pages
1. **Vérifier la branche par défaut** : ouvrez l'onglet **Settings** du dépôt, section **Code and automation > Pages**.
2. Dans **Build and deployment**, choisissez :
   - **Source** : `Deploy from a branch`.
   - **Branch** : sélectionnez `main` (ou la branche souhaitée) et le dossier `/ (root)`.
3. Validez avec **Save**. GitHub Pages lance automatiquement la publication.
4. Patientez quelques minutes que le statut devienne « Your site is published » avec l'URL du site.

## Mettre à jour le site
1. Apportez vos modifications (HTML, CSS, JS, SVG, etc.).
2. Validez vos changements via `git commit` et poussez-les vers la branche configurée (`git push origin main`).
3. GitHub Pages redéploie automatiquement le site. Le cache peut prendre quelques minutes à se rafraîchir.

## Tester en local
1. Clonez le dépôt :
   ```bash
   git clone https://github.com/<organisation>/navette-phoenix-site.git
   cd navette-phoenix-site
   ```
2. Lancez un serveur HTTP local (par exemple avec Python) :
   ```bash
   python3 -m http.server 8000
   ```
3. Ouvrez [http://localhost:8000](http://localhost:8000) dans votre navigateur.

## Structure
- `index.html` : page d'accueil avec accès rapide aux actions principales.
- `tad-reservation.html` : formulaire de réservation TAD (5 €).
- `devis-hors-secteur.html` : demande de devis hors périmètre.
- `infos.html` : informations pratiques et zone desservie.
- `contact.html` : coordonnées et accès aux gares.
- `confidentialite.html` : politique de confidentialité.
- `assets/css/styles.css` : design system et styles globaux.
- `assets/js/main.js` : logique de validation et d'envoi des formulaires.
- `assets/img/logo-phoenix.svg` & `favicon.svg` : identité visuelle en SVG.
- `manifest.webmanifest`, `sitemap.xml`, `robots.txt` : fichiers techniques pour SEO et PWA.

## Personnalisation du webhook
Dans `assets/js/main.js`, vous pouvez définir `WEBHOOK_URL` et `WEBHOOK_TOKEN` pour brancher un service d'automatisation. Laissez ces variables vides pour utiliser l'ouverture d'email classique (`mailto:`).

---
Version du site : **v1.18**
