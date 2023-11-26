package org.example;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.model.*;
import com.stripe.net.ApiResource;
import com.stripe.net.Webhook;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static spark.Spark.port;
import static spark.Spark.get;
public class Main {
    public static void main(String[] args) {

        port(4242);
        get("/webhook",
                ((request, response) -> {
                    String payload = request.body();
                    Event event = null;
                    try {
                        event = ApiResource.GSON.fromJson(payload, Event.class);
                    } catch (Exception e) {
                        response.status(400);
                        return "failure";
                    }
                    // we can choose what will we listen for
                    // that can be some customer or charge or subscription events

//                    System.out.println(event.getId());
//                    System.out.println(event.getAccount());

                    // Secure webhook endpoint
                    // Command for endpoint secret stripe listen --print-secret
                    String endpointSecret = "whsec_2e4b8812f12e1417a33b9ecfb7ebe63e87a2b2228b90dac842872b7677337e70";
                    // Verify webhook signature and extract the event.
                    String signatureHeader = request.headers("Stripe-Signature");
                    try {
                        event = Webhook.constructEvent(
                                payload, signatureHeader, endpointSecret
                        );
                    } catch (Exception e) {
                        // Invalid payload
                        response.status(400);
                        return "failure";
                    }

                    // Deserialize nested object inside the event
                    EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
                    StripeObject stripeObject = null;
                    if (dataObjectDeserializer.getObject().isPresent()) {
                        stripeObject = dataObjectDeserializer.getObject().get();
                    } else {
                        // Deserialization failed, probably due to an API version mismatch.
                        // Refer to the Javadoc documentation on `EventDataObjectDeserializer` for
                        // instructions on how to handle this case, or return an error here.
                    }

                    event.getType(); // the event type, e.g. "charge.failed"
                    // we can use this event.getType() to check what type of event we are getting

                    switch(event.getType()) {
                        case "charge.created":
                            Charge charge = (Charge) stripeObject;
                            System.out.println(charge);
                            break;
                        default:
                            System.out.println("Unhandled event type: " + event.getType());
                    }
                    response.status(200);
                    return "success";
                }));


    }
}