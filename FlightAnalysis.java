import java.io.FileReader;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser; 
import java.text.SimpleDateFormat;

public class FlightAnalysis {
    public static void main(String[] args) {
        String filePath = "tickets.json";
        List<Flight> flights = readFlightsFromFile(filePath);

        if (flights.isEmpty()) {
            System.out.println("Нет данных.");
            return;
        }
        calculateMinFlightTime(flights);
        calculatePriceDifference(flights);
    }

    private static List<Flight> readFlightsFromFile(String filePath) {
        List<Flight> flights = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(new FileReader(filePath));
            JSONArray ticketsArray = (JSONArray) jsonObject.get("tickets");

            for (Object ticketObj : ticketsArray) {
                JSONObject ticket = (JSONObject) ticketObj;
                if ("VVO".equals(ticket.get("origin")) && "TLV".equals(ticket.get("destination"))) {
                    String carrier = (String) ticket.get("carrier");
                    String departureDateTime = ticket.get("departure_date") + " " + ticket.get("departure_time");
                    String arrivalDateTime = ticket.get("arrival_date") + " " + ticket.get("arrival_time");
                    long price = (long) ticket.get("price");

                    Flight flight = new Flight(carrier, departureDateTime, arrivalDateTime, price);
                    flights.add(flight);
                }
            }
        } catch (Exception e) {
            System.out.println("Parcing err" + e.toString());
        }
        return flights;
    }

    private static void calculateMinFlightTime(List<Flight> flights) {
        Map<String, Long> minFlightTimes = new HashMap<>();

        for (Flight flight : flights) {
            long duration = flight.getFlightDuration();
            String carrier = flight.getCarrier();
            minFlightTimes.put(carrier, Math.min(minFlightTimes.getOrDefault(carrier, Long.MAX_VALUE), duration));
        }

        System.out.println("Минимальное время полета для каждого авиаперевозчика:");
        for (Map.Entry<String, Long> entry : minFlightTimes.entrySet()) {
            System.out.println("Авиаперевозчик: " + entry.getKey() + ", Минимальное время: " + entry.getValue() + " минут");
        }
    }

    private static void calculatePriceDifference(List<Flight> flights) {
        List<Long> prices = new ArrayList<>();
        for (Flight flight : flights) {
            prices.add(flight.getPrice());
        }
        Collections.sort(prices);

        double median = (prices.size() % 2 == 0)
                ? (prices.get(prices.size() / 2) + prices.get(prices.size() / 2 - 1)) / 2.0
                : prices.get(prices.size() / 2);

        double average = prices.stream().mapToDouble(Long::doubleValue).average().orElse(0.0);
        double difference = Math.abs(average - median);

        System.out.println("Разница между средней ценой и медианой: " + difference);
    }
}

class Flight {
    private String carrier;
    private String departureDateTime;
    private String arrivalDateTime;
    private long price;
    private SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");

    public Flight(String carrier, String departureDateTime, String arrivalDateTime, long price) {
        this.carrier = carrier;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.price = price;
    }

    public String getCarrier() {
        return carrier;
    }

    public long getPrice() {
        return price;
    }

    public long getFlightDuration() {
        try {
            Date departure = format.parse(departureDateTime);
            Date arrival = format.parse(arrivalDateTime);
            return (arrival.getTime() - departure.getTime()) / (60 * 1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Long.MAX_VALUE;
    }
}
