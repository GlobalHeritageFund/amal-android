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
