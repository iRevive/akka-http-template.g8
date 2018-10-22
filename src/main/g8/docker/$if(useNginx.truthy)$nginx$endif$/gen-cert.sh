# Generate a new root key:
openssl genrsa -out dev-$name_normalized$-CA.key 2048

# Generate a root certificate (enter whatever you'd like at the prompts):
openssl req -x509 -new -nodes -key dev-$name_normalized$-CA.key -days 10000 -out dev-$name_normalized$-CA.crt

# Then generate a key for your server (this is the file referenced by `ssl_certificate_key` in our Nginx configuration):
openssl genrsa -out $name_normalized$-domain.key 2048

# After you type this command, OpenSSL will prompt you to answer a few questions. Write whatever you'd like for the first few,
# but when OpenSSL prompts you to enter the *"Common Name" make sure to type in the domain or IP of your server.*
openssl req -new -key $name_normalized$-domain.key -out dev-$name_normalized$.csr

# Do not enter a challenge password.

# Next, we need to sign the certificate request:
openssl x509 -req -in dev-$name_normalized$.csr -CA dev-$name_normalized$-CA.crt -CAkey dev-$name_normalized$-CA.key -CAcreateserial -out $name_normalized$-domain.crt -days 10000