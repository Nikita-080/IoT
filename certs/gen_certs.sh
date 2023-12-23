openssl genrsa -out app.key 2048
openssl req -key app.key -new -out app.csr
openssl x509 -signkey app.key -in app.csr -req -days 365 -out app.crt
