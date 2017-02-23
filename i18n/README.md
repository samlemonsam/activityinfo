
# Internationalization

The i18n module contains internationalization resources for ActivityInfo, including translations and locale
directives.

## Supported Locales

Supported locales are defined by the 
[Application Locale enumeration](src/main/java/org/activityinfo/i18n/shared/ApplicationLocale.java).

New locales must _also_ be added to 
[I18N.gwt.xml](src/main/java/org/activityinfo/i18n/I18n.gwt.xml).

## Translations

Translations are managed using the [PoEditor](https://poeditor.com/projects/view?id=26801) service. 

Developers should add all user interface strings to either the
[UiConstants interface](src/main/java/org/activityinfo/i18n/shared/UiConstants.java), or to the
[UiMessages interface](src/main/java/org/activityinfo/i18n/shared/UiMessages.java) for user interface strings
that require parameters.

After adding new strings, invoke

```
./gradlew i18n:push
```

to push the new strings to PoEditor. 

Translators should _only_ update translations through PoEditor. Prior to a release, translations can be pulled from
PoEditor into the source by invoking:

```
./gradlew i18n:pull
```

