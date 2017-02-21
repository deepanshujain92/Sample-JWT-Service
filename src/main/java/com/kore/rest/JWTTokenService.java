package com.kore.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/*This method will be available on POST /jwt/api of the application. and body of request should be as below
{
"clientId":"<client_id>",
"url": "<audience>", eg. https://idproxy-qa.kore.com/authorize
"isAnonymous": <true/false>
}

 */
@Path("/jwt")

public class JWTTokenService {

    @POST
    @Path("/api")
    @Produces(MediaType.APPLICATION_JSON)
    public JWTtoken getToken(String request) throws IllegalArgumentException, UnsupportedEncodingException, JSONException, FileNotFoundException, IOException {
        Properties prop = new Properties();
        InputStream input = null;
        try {
            input = new FileInputStream("config.properties");

            // load a properties file
            prop.load(input);

            // get the property value and print it out
           
            JSONObject requestBody = new JSONObject(request);
            String clientId = requestBody.getString("clientId");
            UUID randomUUID = java.util.UUID.randomUUID();
            Calendar cal = Calendar.getInstance();
            cal.setTime(new Date());
            cal.add(Calendar.DATE, 1); //minus number would decrement the days
            Date exp = cal.getTime();
            String jwt = JWT.create()
                    .withIssuedAt(new Date())
                    .withSubject(randomUUID.toString()) //identity - random generated
                    .withIssuer(clientId) //client id
                    .withExpiresAt(exp)
                    .withAudience(requestBody.getString("url")) //audience 
                    .withClaim("isAnonymous", requestBody.getString("isAnonymous"))
                    .sign(Algorithm.HMAC256(prop.getProperty("clientSecret"))); //client secret
            JWTtoken jToken = new JWTtoken();
            jToken.setJwt(jwt);
            jToken.setStatus("success");
            jToken.setDescription("successfully sent jwt");
            return jToken;
        } catch (JWTCreationException exception) {

            //Invalid Signing configuration / Couldn't convert Claims.
            JWTtoken jToken = new JWTtoken();
            jToken.setJwt("");
            jToken.setStatus("failure");
            jToken.setDescription("Exception while creating JWT token");
            return jToken;
        } catch (UnsupportedEncodingException exception) {
            JWTtoken jToken = new JWTtoken();
            jToken.setStatus("failure");
            jToken.setJwt("");
            jToken.setDescription("Exception due to encoding not supported.");
            return jToken;
        } catch (IllegalArgumentException exception) {
            JWTtoken jToken = new JWTtoken();
            jToken.setStatus("failure");
            jToken.setJwt("");
            jToken.setDescription("Exception due to illegal argument.");
            return jToken;
        } catch (JSONException exception) {
            JWTtoken jToken = new JWTtoken();
            jToken.setStatus("failure");
            jToken.setDescription("Exception due to Json format not valid");
            return jToken;
        }

    }

}
