package cat.tecnocampus.fgcstations;

import cat.tecnocampus.fgcstations.application.DTOs.FriendsDTO;
import cat.tecnocampus.fgcstations.domain.Friends;
import cat.tecnocampus.fgcstations.persistence.FriendDAO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
class FgCstationsApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    @Test
    @Order(1)
    void testGetAllUsernames() throws Exception {
        mockMvc.perform(get("/usernames")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3))) //Como se pone el min y max junto?
                .andExpect(jsonPath("$[0].username").value("joanra"))
                .andExpect(jsonPath("$[0].friends").value("manel"))

                .andExpect(jsonPath("$[*].friends", containsInAnyOrder( "manel", "anna", "clara")));
    }

    @Test
    void testGetFriendsUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/friends/alba")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
    @Test
    void testCreateFriend() throws Exception {
        String friends = """
                   {\"username\": \"alba\",
                     \"friends\": \"Oscar\",
                   }
                """;
        FriendsDTO expectedProfile = new FriendsDTO("alba","Oscar"); //Si no espera nada puedo crear un constructor?
        //Ya que necesito pasarle por paramentro los usuarios y amigos

        MvcResult mvcResult = mockMvc.perform(post("/friends")
                        .contentType("application/json")
                        .content(friends))
                .andExpect(status().isCreated())
                .andReturn();
        String actualResponseBody = mvcResult.getResponse().getContentAsString();
        assertThat(actualResponseBody).isEqualToIgnoringWhitespace(
                objectMapper.writeValueAsString(expectedProfile));
    }
    @Test
    void testCreateFriendsWithErrors() throws Exception {
        String friends = """
                   {\"username\": \"alba\",
                     \"friends\": \"Oscar\",
                   } """;

        mockMvc.perform(post("/friends")
                        .contentType("application/json")
                        .content(friends))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.violations").isArray())
                .andExpect(jsonPath("$.violations[*].message", containsInAnyOrder(
                        "must be a well-formed email address",
                        "Nickname must begin with a capital letter. Also only letters are allowed",
                        "size must be between 5 and 10")));
    }

    //Esto Josep me dijo que no se tiene q hacer
  /*  @BeforeEach
    void setUp(){
        FriendDAO friendDao;
    }

    @Test
    void testFriendsSizeLowerLetters() {
        // Convierte el DTO en una cadena (puedes ajustar esto según tu implementación)
        String username  = friendDao.getUsername();

        // Verificar que el tamaño de la cadena esté entre 3 y 255 caracteres.
        assertTrue(username.length() >= 3 && username.length() <= 255);

        // Verificar que todos los campos de la cadena contengan solo letras minúsculas.
        assertTrue(username.matches("^[a-z]+$"));

    }*/

}
