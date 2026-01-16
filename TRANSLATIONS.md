# Translate Yearly Progress
Yearly Progress is an Android app that helps users track and visualize their progress
across days, weeks, months, and years.

This app uses Android string resources (`strings.xml`) for translations.

---

## How Android translations work

Each language has its own resource directory:

- Default (English):  
  `app/src/main/res/values/strings.xml`

- Other languages:  
  `app/src/main/res/values-<language-code>/strings.xml`

Examples:
- `values-hi/strings.xml`
- `values-fr/strings.xml`
- `values-es/strings.xml`
- `values-pt-rBR/strings.xml`

Language codes must follow Android and ISO standards.

---

## How to add a new language

1. Fork this repository
2. Copy:
   `app/src/main/res/values/strings.xml`
3. Create a new folder:
   `app/src/main/res/values-<language-code>/`
4. Paste the copied file as `strings.xml`
5. Translate the string **values only**
6. Commit your changes
7. Open a Pull Request

---

## Translation rules (very important)

- Do **not** change string names
- Do **not** remove any strings
- Do **not** add new strings
- Do **not** translate placeholders such as `%1$s`, `%d`, `%f`
- Do **not** translate strings marked with `translatable="false"`
- Translate only the text between `<string>` tags

Incorrect:
```xml
<string name="days_completed">%d days</string>
````

Correct:

```xml
<string name="days_completed">%d दिन</string>
```

---

## Plurals

If the app uses `plurals`, translate each quantity correctly:

```xml
<plurals name="days_remaining">
    <item quantity="one">%d day remaining</item>
    <item quantity="other">%d days remaining</item>
</plurals>
```

Do not remove or rename quantities.

---

## Testing (recommended)

* Run the app
* Change the device language to the one you added
* Ensure the app launches without crashing

---

## Submitting your translation

When opening a Pull Request, include:

* Language name
* Language code

Example:

```
Added Hindi translation (hi)
```

---

## Credits

All translators will be credited inside the app under:

**Settings → About → Translators**

