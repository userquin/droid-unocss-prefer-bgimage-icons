# https://github.com/antfu/unocss/issues/52

You will need:

- Android Studio Arctic Fox | 2020.3.1 Patch 2
- Add Android 11 (31) from SDK Manager

The native Android application will run on devices from 21 (Lollipop) and the target is Android 11 (31).

You will need also this repo: https://github.com/userquin/unocss-prefer-bgimage-icons.

To build the web, you will need latest `pnpm` and `node 14+`.

To build the webapp, first install dependencies `pnpm install` then `pnpm run build`.

Copy dist folder content and paste on `app/src/main/assets/www` directory (stop the android app).

