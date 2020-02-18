FROM node:alpine

USER node

RUN mkdir /home/node/workspace && chown node:node /home/node/workspace

WORKDIR /home/node/workspace

CMD ["sh"]
