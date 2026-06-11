# DosageCalc

Android application for dermatological drug dosage calculation. Designed for medical professionals who need to compute, review, and track patient-specific doses at the point of care.

## What it does

DosageCalc guides the clinician through a three-step flow - drug selection, patient data entry, and dose result - and stores every calculation in a local encrypted database for future reference.

### Dose calculation

The calculator supports three formula types:

- **Per kilogram** (mg/kg or mcg/kg): dose multiplied by patient weight.
- **Per square metre** (mg/m2): dose multiplied by body surface area, calculated with the Mosteller or Du Bois formula (selectable in settings).
- **Fixed dose**: a predetermined dose independent of patient anthropometrics.

If the computed dose exceeds the drug's configured safety ceiling, it is automatically capped and the user is notified before the result is accepted.

### Drug catalog

A built-in catalog is distributed with the app, sourced exclusively from RCP, AIFA, and WHO references. Each entry includes indication, dosage formula, contraindications, side effects, and clinical alerts.

Clinicians can also create, edit, and delete custom drug entries, which are stored locally alongside the built-in catalog.

Drug-drug interaction detection is available for the built-in catalog, with severity levels shown on the result screen.

### Patient management

Patient records store weight, height, age, sex, and flags for renal and hepatic impairment. These values are pre-filled into the dose calculator when a patient is selected, reducing data-entry time and the risk of transcription errors.

Dose history is stored per patient and can be reviewed at any time from the patient detail screen.

### History and analytics

The history screen lists every calculation with its inputs and result. Records can be searched by drug name or patient and exported to CSV.

The analytics screen shows a dose-trend chart for a selected drug and patient, and a category-distribution summary across all calculations.

### Export

Calculation results can be exported as a PDF report for inclusion in clinical documentation or shared directly from the device. History can be exported as CSV.

### Reminders

Dose reminders can be scheduled with daily, weekly, or monthly recurrence directly from the result screen. Active reminders are listed in a dedicated screen and can be cancelled individually.

Two home-screen widgets are available (via Jetpack Glance): one showing the last drug calculated, one showing the time of the next scheduled reminder.

## Supported devices

- Minimum Android version: 8.0 (API 26)
- Target Android version: 15 (API 35)
- Phone portrait and landscape, foldable, and tablet layouts are all supported.

## Building

```bash
# Debug APK
./gradlew :app:assembleDebug

# Unit tests
./gradlew :app:test

# Lint
./gradlew :app:lint
```

## License

To be defined.
