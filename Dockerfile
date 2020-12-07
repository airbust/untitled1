FROM java:8
WORKDIR /app/
COPY ./* ./
RUN javac Lox.java