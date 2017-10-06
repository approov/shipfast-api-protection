## Build and Run

```bash
openssl genrsa -out localhost.key 2048
openssl req -new -days 365 -key localhost.key -out localhost.pem
openssl x509 -req -days 365 -in localhost.pem -signkey localhost.key -extfile ./android_cert_options.txt -out localhost.crt
openssl x509 -inform PEM -outform DER -in localhost.crt -out localhost.der.crt
adb push localhost.der.crt /sdcard
npm install
sudo node server.js
```