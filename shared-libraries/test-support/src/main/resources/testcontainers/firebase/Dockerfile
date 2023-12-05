FROM andreysenov/firebase-tools:12.4.6-node-18-alpine

COPY firebase.json /home/node/firebase.json

EXPOSE 9099

CMD ["firebase", "--project=cooltra-unit-test", "emulators:start", "--only", "auth"]
