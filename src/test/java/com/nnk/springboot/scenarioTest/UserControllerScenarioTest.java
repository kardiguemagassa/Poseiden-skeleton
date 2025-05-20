package com.nnk.springboot.scenarioTest;

import com.nnk.springboot.config.SecurityConfig;
import com.nnk.springboot.domain.Users;
import com.nnk.springboot.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(SecurityConfig.class)
public class UserControllerScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Users createAndSaveTestUser() {
        Users user = new Users();
        user.setUsername("testuser");
        user.setPassword(passwordEncoder.encode("Password123!"));
        user.setFullname("Test User");
        user.setRole("USER");
        return userRepository.save(user);
    }

    private MockHttpServletRequestBuilder authenticatedRequest(MockHttpServletRequestBuilder builder) {
        return builder.with(user("admin").password("password").roles("ADMIN"));
    }

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUserScenario_success() throws Exception {
        // 1. Accès au formulaire d'ajout (authentifié)
        mockMvc.perform(authenticatedRequest(get("/user/add")))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"));

        // 2. Soumission du formulaire avec des données valides (authentifié)
        mockMvc.perform(authenticatedRequest(post("/user/validate"))
                        .param("username", "newuser")
                        .param("password", "ValidPass123!")
                        .param("fullname", "New User")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"))
                .andExpect(flash().attributeExists("success"));

        // 3. Vérification en base de données
        assertEquals(1, userRepository.count());
        assertNotNull(userRepository.findByUsername("newuser"));
    }


    @Test
    void createUserScenario_duplicateUser() throws Exception {

        Users existingUser = new Users();
        existingUser.setUsername("existing");
        existingUser.setPassword(passwordEncoder.encode("Pass123!"));
        existingUser.setFullname("Existing User");
        existingUser.setRole("USER");
        userRepository.save(existingUser);

        // 2. Tentative de création du même utilisateur (avec authentification)
        mockMvc.perform(post("/user/validate")
                        .with(user("admin").roles("ADMIN")) // Authentification mockée
                        .param("username", "existing")
                        .param("password", "NewPass123!")
                        .param("fullname", "Existing User")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"))
                .andExpect(model().attributeHasFieldErrors("user", "username"));

        // 3. Vérification qu'un seul utilisateur existe
        assertEquals(1, userRepository.count());
    }


    @Test
    void updateUserScenario_success() throws Exception {
        // 1. Création d'un utilisateur initial
        Users user = createAndSaveTestUser();
        int userId = user.getId();

        // 2. Accès au formulaire de mise à jour (authentifié)
        mockMvc.perform(authenticatedRequest(get("/user/update/" + userId)))
                .andExpect(status().isOk())
                .andExpect(view().name("user/update"));

        // 3. Soumission des modifications (authentifié)
        mockMvc.perform(authenticatedRequest(post("/user/update/" + userId))
                        .param("username", "updateduser")
                        .param("password", "NewPass123!")
                        .param("fullname", "New Name")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list"))
                .andExpect(flash().attributeExists("success"));

        // 4. Vérification des modifications
        Users updatedUser = userRepository.findById(userId).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());
        assertEquals("New Name", updatedUser.getFullname());
        assertEquals("ADMIN", updatedUser.getRole());
    }

    @Test
    void createUserScenario_invalidInput_shouldFail() throws Exception {
        mockMvc.perform(authenticatedRequest(post("/user/validate"))
                        .param("username", "")
                        .param("password", "")
                        .param("fullname", "")
                        .param("role", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("user/add"))
                .andExpect(model().attributeHasFieldErrors("user", "username", "password", "fullname", "role"));
    }

    @Test
    void deleteUserScenario_success() throws Exception {
        // 1. Création d'un utilisateur
        Users user = new Users();
        user.setUsername("todelete");
        user.setPassword(passwordEncoder.encode("Pass123!")); // Encodage ajouté
        user.setFullname("To Delete");
        user.setRole("USER");
        user = userRepository.save(user);
        int userId = user.getId();

        // 2. Suppression de l'utilisateur (avec authentification)
        mockMvc.perform(authenticatedRequest(get("/user/delete/" + userId)))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/user/list?success=user.deleted"));

        // 3. Vérification de la suppression
        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void fullCrudScenario() throws Exception {
        // 1. Liste initiale vide (avec authentification)
        mockMvc.perform(authenticatedRequest(get("/user/list")))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("users"))
                .andExpect(model().attribute("users", hasSize(0)));

        // 2. Création d'un utilisateur (avec authentification)
        mockMvc.perform(authenticatedRequest(post("/user/validate"))
                        .param("username", "cruduser")
                        .param("password", "CrudPass123!")
                        .param("fullname", "CRUD User")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection());

        // 3. Vérification dans la liste (avec authentification)
        mockMvc.perform(authenticatedRequest(get("/user/list")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("users", hasSize(1)));

        Users user = userRepository.findByUsername("cruduser")
                .orElseThrow(() -> new AssertionError("User not found"));
        assertNotNull(user);

        // 4. Mise à jour (avec authentification)
        mockMvc.perform(authenticatedRequest(post("/user/update/" + user.getId()))
                        .param("username", "updateduser")
                        .param("password", "UpdatedPass123!")
                        .param("fullname", "Updated User")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection());

        // 5. Vérification mise à jour
        Users updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertEquals("updateduser", updatedUser.getUsername());

        // 6. Suppression (avec authentification)
        mockMvc.perform(authenticatedRequest(get("/user/delete/" + user.getId())))
                .andExpect(status().is3xxRedirection());

        // 7. Vérification suppression
        assertFalse(userRepository.existsById(user.getId()));
    }
}
