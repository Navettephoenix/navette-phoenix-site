# Navette Phoenix

Ce dépôt contient l&#39;application mobile Android *Navette Phoenix* ainsi que les ressources web historiques.

## Prérequis

* [Android Studio](https://developer.android.com/studio) Iguana ou plus récent.
* JDK 17 ou 21.
* Un Android SDK configuré (API 34 recommandé).

## Ouvrir le projet

1. Cloner ce dépôt.
2. Dans Android Studio, choisir **Open** et sélectionner le dossier `navette-phoenix-site`.
3. Laisser Android Studio effectuer la synchronisation Gradle.

## Compilation en ligne de commande

```bash
# Depuis la racine du projet
gradle wrapper --gradle-version 8.7.2   # Première fois uniquement si vous souhaitez générer le wrapper
./gradlew assembleDebug                 # Compile l&#39;APK de debug
```

> **Remarque :** Si le SDK Android n&#39;est pas détecté automatiquement, configurez la variable `ANDROID_HOME` ou éditez le fichier `local.properties` pour y renseigner `sdk.dir=/chemin/vers/votre/sdk`.

## Structure principale

```
navette-phoenix-site/
├─ app/
│  ├─ src/main/java/com/navettephoenix/
│  │  ├─ data/        # Modèles et faux dépôts de données
│  │  ├─ domain/      # Cas d&#39;usage (business)
│  │  └─ ui/          # Activités et état d&#39;interface
│  └─ src/main/res/   # Layouts, thèmes et ressources
├─ build.gradle       # Configuration Gradle racine
├─ settings.gradle    # Déclaration des modules
└─ README.md
```

## Validation

* Synchronisation Gradle : `gradle --refresh-dependencies help`
* Compilation : `./gradlew assembleDebug`

Des tâches supplémentaires (tests unitaires, lint, etc.) pourront être ajoutées par l&#39;équipe au fur et à mesure.
