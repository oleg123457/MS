package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.MappingsRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithMockUser;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WithMockUser(username = "user", password = "123", authorities = "ROLE_MODERATOR")
@SpringBootTest
public class MyTest extends BaseIntegrationTest {

    @MockBean
    private Keycloak keycloakClient;
    @Mock
    private RealmResource realmResource;
    @Mock
    private UsersResource usersResource;
    @Mock
    private Response response;
    @Mock
    private UserResource userResource;
    @Mock
    private UserRepresentation userRepresentation;
    @Mock
    private RoleMappingResource roleMappingResource;
    @Mock
    private UserMapper userMapper;
    @Mock
    private UserResponse userResponse;
    @Mock
    private MappingsRepresentation mappingsRepresentation;
    UserRequest userRequest = new UserRequest("Grigory", "123@y.ru", "12345", "Grisha", "Rururu");
    UserRequest badUserRequest = new UserRequest("G", "123@y.ru", "12345", "Grisha", "Rururu");

    @Test
    public void testHelloController() throws Exception {

        mvc.perform(get("/api/users/hello"))    //посылаем запрос
                .andExpect(status().is2xxSuccessful())
                .andDo(print())
                .andExpect(content().string(containsString("user")));
    }

    @Test
    public void testCreateUser() throws Exception {

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenReturn(response);
        when(response.getStatusInfo()).thenReturn(Response.Status.CREATED);

        mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andExpect(status().isOk())
                .andDo(print());

    }
    @Test
    public void testCreateBadUser() throws Exception {

        mvc.perform(requestWithContent(post("/api/users"), badUserRequest))
                .andExpect(status().is4xxClientError())
                .andDo(print());

    }

    @Test
    public void testCreateUserException() throws Exception {
        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.create(ArgumentMatchers.any(UserRepresentation.class))).thenThrow(new WebApplicationException());

        mvc.perform(requestWithContent(post("/api/users"), userRequest))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

    @Test
    public void testGetUserById() throws Exception {
        UUID userId = UUID.randomUUID();

        List<RoleRepresentation> userRoles = new ArrayList<>();

        List<GroupRepresentation> userGroups = new ArrayList<>();

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(userId))).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(userRoles);

        when(userResource.groups()).thenReturn(userGroups);

        when(userMapper.userRepresentationToUserResponse(userRepresentation, userRoles, userGroups)).thenReturn(userResponse);

        mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isOk())
                .andDo(print());
    }

    @Test
    public void testGetUserByIdException() throws Exception {
        UUID userId = UUID.randomUUID();

        List<RoleRepresentation> userRoles = new ArrayList<>();

        List<GroupRepresentation> userGroups = new ArrayList<>();

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(String.valueOf(userId))).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        when(userResource.roles()).thenReturn(roleMappingResource);
        when(roleMappingResource.getAll()).thenReturn(mappingsRepresentation);
        when(mappingsRepresentation.getRealmMappings()).thenReturn(userRoles);

        when(userResource.groups()).thenReturn(userGroups);

        when(usersResource.get(String.valueOf(userId))).thenThrow(new BackendResourcesException("ID not found", HttpStatus.BAD_REQUEST));

        when(userMapper.userRepresentationToUserResponse(userRepresentation, userRoles, userGroups)).thenReturn(userResponse);

        mvc.perform(get("/api/users/{id}", userId))
                .andExpect(status().isInternalServerError())
                .andDo(print());
    }

}
