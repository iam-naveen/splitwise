## SETUP INSTRUCTIONS

> Prefered Editor: Visual Studio Code

### Setup the Database

- Start your MYSQL Database Server
- Create a database
- Use the provided [DB_CREATE_QUERIES](https://github.com/iam-naveen/splitwise/blob/master/DB_CREATE_QUERIES.sql "SQL FILE") file and create the Tables
- optional: seed it with appropriate data

### Establish Connection

- In [Main.java](https://github.com/iam-naveen/splitwise/blob/master/Main.java "Main.java")

```java
    public static void main(String[] args) {

        // Provide your Database url here
        String url = "jdbc:mysql://localhost:3306/splitwise";
        // Provide your username here
        String username = "root";
        // Provide your password here
        String password = "";

        try (Connection conn = DriverMa...
```

- After changing the connection details as mentioned above you are good to go.
- Start the application and follow the Console UI.

> [!NOTE]  
> Make sure to add the JDBC mysql connector while running the application. It is added to class path by default if you use VSCode.
> If you are using some other editor then add the [CONNECTOR](https://github.com/iam-naveen/splitwise/blob/master/lib/mysql-connector-j-8.0.31.jar "MySQL Connector") to your class path manually.

## ABOUT THE APPLICATION

A Simple CLI application that can log users in and create groups and allow them to share expenses among the group members.
