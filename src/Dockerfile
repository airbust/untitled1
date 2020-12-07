FROM java:8
WORKDIR /app/
COPY ./com ./com
RUN pwd
RUN ls
RUN javac -cp . ./com/craftinginterpreters/lox/Lox.java
RUN javac -cp . ./com/PrintData.java