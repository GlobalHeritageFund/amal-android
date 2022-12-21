# Amal Android

[Amal](http://amal.global/) is a mobile and web application designed for the rapid impact assessment of damaged heritage areas, buildings, or artifacts. By collecting data in the immediate aftermath of a disaster, AMAL preserves crucial information that can be used to repair or reconstruct damaged heritage.

This repository hosts the code for running the Amal Android app.

## Requirements

* Android Studio
* Kotlin
* Android 6.0 (Marshmallow), API Level 23

## How to build the app

1. Clone the repository.
2. Get the `google-services.json` file from a developer and move it to the root of the repository. It should be gitignored.
3. Get the `google_maps_api.xml` file from a developer. Copy it to the following locations:

	```
	app/src/release/res/values/google_maps_api.xml
	app/src/debug/res/values/google_maps_api.xml
	```
	There should be placeholders there with dummy content.
4. Build gradle.
5. Build the app.

## Animating Principles

### Backward compatibility

The Amal app is designed to run on as many Android devices as possible. Because the phones that may be used target regions may be cheaper and less powerful than the flagship devices available to developers, the minimum API level for this app is set as low as the Play Store will allow.

While this creates some small limitations for the developers of the app, being able to use the app on many devices allows for maximal adoption.

### Small binary size

Because the target regions may have slower internet access than users of more typical phone apps, the app and any updates should be as small as possible. Images should be compressed and optimized, and other assets should be minimized where possible.

### User friendliness

Because the app is able to be used by laypeople in addition to professionals, the app should be user friendly. Where training is provided, it should be focused on the data gathering techniques, rather than the incidental complexity of the app. Platform conventions should be followed and only be broken for very good reasons.
