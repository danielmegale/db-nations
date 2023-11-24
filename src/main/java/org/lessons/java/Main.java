package org.lessons.java;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        String dbUrl = "jdbc:mysql://localhost:3306/db-nations";
        String username = "root";
        String password = "root";

        try (Scanner scanner = new Scanner(System.in);
             Connection connection = DriverManager.getConnection(dbUrl, username, password)) {

            System.out.print("Search: ");
            String searchString = scanner.nextLine();

            String query = "SELECT countries.name AS nome_nazione, " +
                    "countries.country_id AS id_nazione, " +
                    "regions.name AS nome_regione, " +
                    "continents.name AS nome_continente " +
                    "FROM countries " +
                    "JOIN regions ON countries.region_id = regions.region_id " +
                    "JOIN continents ON regions.continent_id = continents.continent_id " +
                    "WHERE countries.name LIKE ? " +
                    "ORDER BY countries.name";

            try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                preparedStatement.setString(1, "%" + searchString + "%");

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    System.out.printf("%-5s %-30s %-30s %-20s%n", "ID", "Country", "Regione", "Continente");
                    System.out.println("--------------------------------------------------------------");
                    while (resultSet.next()) {
                        int idNazione = resultSet.getInt("id_nazione");
                        String nomeNazione = resultSet.getString("nome_nazione");
                        String nomeRegione = resultSet.getString("nome_regione");
                        String nomeContinente = resultSet.getString("nome_continente");

                        System.out.printf("%-5d %-30s %-30s %-20s%n", idNazione, nomeNazione, nomeRegione, nomeContinente);
                    }
                }
            }

            System.out.print("\nChoose a country id: ");
            int countryId = scanner.nextInt();
            scanner.nextLine();  // Consuma il carattere di nuova linea rimasto nel buffer

            System.out.println("\nDetails for Country:");
            getLanguagesForCountry(connection, countryId);
            getRecentStatisticsForCountry(connection, countryId);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void getLanguagesForCountry(Connection connection, int countryId) throws SQLException {
        String languageQuery = "SELECT languages.language " +
                "FROM languages " +
                "JOIN country_languages ON languages.language_id = country_languages.language_id " +
                "WHERE country_languages.country_id = ?";
        try (PreparedStatement languageStatement = connection.prepareStatement(languageQuery)) {
            languageStatement.setInt(1, countryId);
            try (ResultSet languageResultSet = languageStatement.executeQuery()) {
                System.out.println("\nLanguages:");
                while (languageResultSet.next()) {
                    String language = languageResultSet.getString("language");
                    System.out.print(language+",");
                }
            }
        }
    }
	
    private static void getRecentStatisticsForCountry(Connection connection, int countryId) throws SQLException {
        String statisticsQuery = "SELECT * FROM country_stats WHERE country_id = ? ORDER BY year DESC LIMIT 1";
        try (PreparedStatement statisticsStatement = connection.prepareStatement(statisticsQuery)) {
            statisticsStatement.setInt(1, countryId);
            System.out.println("Executing query: " + statisticsStatement);
            try (ResultSet statisticsResultSet = statisticsStatement.executeQuery()) {
                System.out.println("\nStatistiche più recenti per la country:");
                if (statisticsResultSet.next()) {
                    int year = statisticsResultSet.getInt("year");
                    int population = statisticsResultSet.getInt("population");
                    long gdp = statisticsResultSet.getLong("gdp"); // Cambiato da int a long

                    System.out.println("Anno: " + year + "\n"
                            +"Popolazione: " + population +"\n"
                            +"GDP: " + gdp);
                } else {
                    System.out.println("Nessuna statistica trovata per la country con ID: " + countryId);
                }
            }
        }
    }


}