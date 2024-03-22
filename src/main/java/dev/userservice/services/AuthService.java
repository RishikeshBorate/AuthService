package dev.userservice.services;

import dev.userservice.dtos.UserDto;
import dev.userservice.exceptions.UserDoesNotExistsException;
import dev.userservice.models.Session;
import dev.userservice.models.SessionStatus;
import dev.userservice.models.User;
import dev.userservice.repositories.SessionRepository;
import dev.userservice.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMapAdapter;

import javax.crypto.SecretKey;
import java.util.*;

@Service
public class AuthService {
    private UserRepository userRepository;
    private SessionRepository sessionRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthService(UserRepository userRepository, SessionRepository sessionRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public ResponseEntity<UserDto> login(String email, String password) throws UserDoesNotExistsException {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            throw new UserDoesNotExistsException("User with given userId does not exists.");
        }

        User user = userOptional.get();

        /*Long UserId = user.getId();
        List<Session> sessionList = sessionRepository.findAllByUserId(UserId) ;

        if(sessionList.size()>=2){
            throw new SessionLimitExceededException("Your session limit exceeded please log out from any session");
        }
*/
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())) {
            //throw an exception
            throw new RuntimeException("Wrong password entered");
        }

        //Generating the token
        //String token = RandomStringUtils.randomAlphanumeric(30);  string token used for learning purpose

      // Create a test key suitable for the desired HMAC-SHA algorithm:
        MacAlgorithm alg = Jwts.SIG.HS256; //or HS384 or HS256
        SecretKey key = alg.key().build();

        //String message = "Hello World!";

        // create json map to send msg or payload

        Map<String , Object> jsonMap = new HashMap<>() ;

        jsonMap.put("email" , user.getEmail()) ;
        jsonMap.put("roles" , List.of(user.getRoles())) ;
        jsonMap.put("createdAt" , new Date()) ;
        jsonMap.put("expiryAt" , DateUtils.addDays(new Date() , 30)) ;

        //byte[] content = message.getBytes(StandardCharsets.UTF_8);

// Create the compact JWS:
        //String jws = Jwts.builder().content(content, "text/plain").signWith(key, alg).compact();

// Parse the compact JWS:
        //content = Jwts.parser().verifyWith(key).build().parseSignedContent(jws).getPayload();

        //assert message.equals(new String(content, StandardCharsets.UTF_8));

        // parsing not required directly create jws token using jsonMap

        String jws = Jwts.builder().
                claims(jsonMap).
                signWith(key,alg).
                compact();

        Session session = new Session();
        session.setSessionStatus(SessionStatus.ACTIVE);
       // session.setToken(token);
        session.setToken(jws);
        session.setUser(user);
        //session.setExpiringAt(//current time + 30 days);
        sessionRepository.save(session);

        UserDto userDto = new UserDto();
        userDto.setEmail(email);

        MultiValueMapAdapter<String, String> headers = new MultiValueMapAdapter<>(new HashMap<>());
        headers.add(HttpHeaders.SET_COOKIE, "auth-token:" + jws);

        ResponseEntity<UserDto> response = new ResponseEntity<>(userDto, headers, HttpStatus.OK);

        return response;
    }

    public ResponseEntity<Void> logout(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        session.setSessionStatus(SessionStatus.ENDED);

        sessionRepository.save(session);

        return ResponseEntity.ok().build();
    }


    public UserDto signUp(String email, String password) {
        User user = new User();
        user.setEmail(email);
        user.setPassword(bCryptPasswordEncoder.encode(password)); // We should store the encrypted password in the DB for a user.

        User savedUser = userRepository.save(user);

        return UserDto.from(savedUser);
    }


    public SessionStatus validate(String token, Long userId) {
        Optional<Session> sessionOptional = sessionRepository.findByTokenAndUser_Id(token, userId);

        if (sessionOptional.isEmpty()) {
            return null;
        }

        Session session = sessionOptional.get();

        if (!session.getSessionStatus().equals(SessionStatus.ACTIVE)) {
            return SessionStatus.ENDED;
        }

        Date currentTime = new Date();
        if (session.getExpiringAt().before(currentTime)) {
            return SessionStatus.ENDED;
        }

        //JWT Decoding.
       /* Jws<Claims> jwsClaims = Jwts.parser().build().parseSignedClaims(token);

        // Map<String, Object> -> Payload object or JSON
        String email = (String) jwsClaims.getPayload().get("email");
        List<Role> roles = (List<Role>) jwsClaims.getPayload().get("roles");
        Date createdAt = (Date) jwsClaims.getPayload().get("createdAt");
        */
//        if (restrictedEmails.contains(email)) {
//            //reject the token
//        }

        return SessionStatus.ACTIVE;
    }
}

/*

eyJjdHkiOiJ0ZXh0L3BsYWluIiwiYWxnIjoiSFMyNTYifQ.
SGVsbG8gV29ybGQh.
EHQJBVvni4oDe_NEqnecIwNmOTUe_7Hs_jVW_XT-b1o

*/

/*
Task-1 : Implement limit on number of active sessions for a user.
Task-2 : Implement login workflow using the token details with validation of expiry date.
 */
