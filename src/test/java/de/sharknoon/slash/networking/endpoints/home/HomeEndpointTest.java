package de.sharknoon.slash.networking.endpoints.home;

import com.google.gson.*;
import de.sharknoon.slash.database.DB;
import de.sharknoon.slash.database.models.User;
import de.sharknoon.slash.networking.endpoints.TestSession;
import de.sharknoon.slash.networking.endpoints.login.*;
import de.sharknoon.slash.networking.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.websocket.Session;
import java.lang.reflect.Modifier;
import java.time.LocalDateTime;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;

class HomeEndpointTest {
    private static final Gson gson = new GsonBuilder()
            .excludeFieldsWithoutExposeAnnotation()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
            .registerTypeAdapter(ObjectId.class, new ObjectIdConverter())
            .excludeFieldsWithModifiers(Modifier.STATIC)
            .create();
    
    private static final User user = new User();
    private static final LoginEndpoint le = new LoginEndpoint();
    private static String sendText = "";
    private static final Session s = new TestSession(t -> sendText = t);
    
    private static void registerUser() {
        user.id = new ObjectId();
        user.username = UUID.randomUUID().toString().substring(0, 15);
        user.sessionIDs = new HashSet<>();
        user.salt = BCrypt.gensalt();
        user.registrationDate = LocalDateTime.now().withNano(0);
        user.password = BCrypt.hashpw("123456", user.salt);
        user.email = user.username + "@web.de";
        user.deviceIDs = new HashSet<>();
        
        DB.register(user);
    }
    
    private static void loginUser(Session s) {
        le.onOpen(s);
        LoginMessage lm = new LoginMessage();
        lm.setUsernameOrEmail(user.email);
        lm.setPassword("123456");
        lm.setDeviceID("123456789");
        le.onMessage(s, new Gson().toJson(lm));
        LoginAnswer la = new Gson().fromJson(sendText, LoginAnswer.class);
        user.sessionIDs.add(la.sessionid);
    }
    
    @BeforeAll
    static void setUp() {
        registerUser();
        loginUser(s);
    }
    
    @AfterAll
    static void tearDown() {
        DB.unregister(user);
    }
    
    
    @Test
    void test_getHomeStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        StatusAndSessionIDMessage sasm = new StatusAndSessionIDMessage();
        sasm.setSessionid(UUID.randomUUID().toString());
        sasm.setStatus(Status.GET_HOME);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(sasm));
        
        // Wrong session id
        Assertions.assertEquals("{\"status\":\"NO_LOGIN_OR_TOO_MUCH_DEVICES\",\"messageType\":\"You are either not logged in or using more than 5 devices\"}", sendText);
        
        // Correct session id
        sasm.setSessionid(user.sessionIDs.iterator().next());
        he.onMessage(s, gson.toJson(sasm));
        Assertions.assertEquals("{\"status\":\"OK_HOME\",\"projects\":[],\"chats\":[]}", sendText);
    }
    
    @Test
    void test_getUserStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        GetUserMessage getUserMessage = new GetUserMessage();
        getUserMessage.setSessionid(user.sessionIDs.iterator().next());
        getUserMessage.setStatus(Status.GET_USER);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(getUserMessage));
        // ToDo
    }
    
    @Test
    void test_getChatStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        GetChatMessage getChatMessage = new GetChatMessage();
        getChatMessage.setSessionid(UUID.randomUUID().toString());
        getChatMessage.setStatus(Status.GET_CHAT);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(getChatMessage));
        // ToDo
    }
    
    @Test
    void test_addProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setSessionid(user.sessionIDs.iterator().next());
        addProjectMessage.setStatus(Status.ADD_PROJECT);
        addProjectMessage.setProjectName("");
        he.onOpen(s);
        he.onMessage(s, gson.toJson(addProjectMessage));
        // Empty project name
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_NAME\",\"description\":\"The project name doesn\\u0027t match the specifications\"}", sendText);
        
        String projectName = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectName(projectName);
        he.onMessage(s, gson.toJson(addProjectMessage));
        //Empty Description
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_DESCRIPTION\",\"description\":\"The project description doesn\\u0027t match the specifications\"}", sendText);
    
        String projectDescription = UUID.randomUUID().toString().substring(0, 15);
        addProjectMessage.setProjectDescription(projectDescription);
        he.onMessage(s, gson.toJson(addProjectMessage));
        Assertions.assertTrue(sendText.startsWith("{\"status\":\"OK_PROJECT\",\"project\":"));
        final String projectStatus = sendText;
        
        HomeEndpoint.ProjectResponse response = gson.fromJson(sendText, HomeEndpoint.ProjectResponse.class);
        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setProjectID(response.project.id.toString());
        getProjectMessage.setStatus(Status.GET_PROJECT);
        getProjectMessage.setSessionid(user.sessionIDs.iterator().next());
        he.onMessage(s, gson.toJson(getProjectMessage));
        
        Assertions.assertEquals(projectStatus, sendText);
        
        DB.leakDatabase().getCollection("projects").deleteMany(eq("name", projectName));
    }
    
    @Test
    void test_getProjectStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        GetProjectMessage getProjectMessage = new GetProjectMessage();
        getProjectMessage.setSessionid(user.sessionIDs.iterator().next());
        getProjectMessage.setStatus(Status.GET_PROJECT);
        getProjectMessage.setProjectID(StringUtils.EMPTY);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(getProjectMessage));
        
        Assertions.assertEquals("{\"status\":\"WRONG_PROJECT_ID\",\"description\":\"The specified projectID doesn\\u0027t conform to the right syntax\"}", sendText);
    
        getProjectMessage.setProjectID("5be0b0dd0fb4e53cc82294f3");
        he.onMessage(s, gson.toJson(getProjectMessage));
        Assertions.assertEquals("{\"status\":\"NO_PROJECT_FOUND\",\"description\":\"No project with the specified id was found\"}", sendText);
    }
    
    @Test
    void test_addMessageStatus() {
        HomeEndpoint he = new HomeEndpoint();
    
        AddProjectMessage addProjectMessage = new AddProjectMessage();
        addProjectMessage.setSessionid(user.sessionIDs.iterator().next());
        addProjectMessage.setStatus(Status.ADD_PROJECT_MESSAGE);
        he.onOpen(s);
        he.onMessage(s, gson.toJson(addProjectMessage));
        // ToDo
    }
    
    private class LoginAnswer {
        String sessionid;
    }
}
