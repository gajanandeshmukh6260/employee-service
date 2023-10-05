package com.happiest.assignment.es.service;

import com.happiest.assignment.es.dao.UserInfoRepository;
import com.happiest.assignment.es.entity.UserInfo;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.Silent.class)
public class UserInfoServiceTest {


    @Mock
    private UserInfoRepository repository;

    @InjectMocks
    private UserInfoService userDetailsService;

    @BeforeEach
    public void setUp(){
        userDetailsService=new UserInfoService();
    }

    @Test
    public void testLoadUserByUsername() {
        String username = "Gajanan";
        UserInfo userInfo = new UserInfo();
        userInfo.setName(username);
        userInfo.setPassword("password");
        userInfo.setRoles("ROLE_USER");

        when(repository.findByName(username)).thenReturn(Optional.of(userInfo));
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        assertNotNull(userDetails);
        assertEquals(username, userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_USER")));

    }

    @Test
    public void TestIsAccountNonExpired(){

        UserInfo userInfo=getUserInfo();
        UserInfoDetails userInfoDetails=new UserInfoDetails(userInfo);
        assertTrue(userInfoDetails.isAccountNonExpired());
    }

    private UserInfo getUserInfo(){
        UserInfo userInfo=new UserInfo();
        userInfo.setName("user123");
        userInfo.setPassword("password");
        userInfo.setRoles("ROLE_ADMIN");
        userInfo.setEmail("abc@gmail.com");
        return userInfo;
    }

    @Test
    public void TestIsAccountNonLocked(){
        UserInfo userInfo=getUserInfo();
        UserInfoDetails userInfoDetails=new UserInfoDetails(userInfo);
        assertTrue(userInfoDetails.isAccountNonLocked());

    }

    @Test
    public void TestisCredentialsNonExpired(){
        UserInfo userInfo=getUserInfo();
        UserInfoDetails userInfoDetails=new UserInfoDetails(userInfo);
        assertTrue(userInfoDetails.isCredentialsNonExpired());

    }

    @Test
    public void TestIsEnabled(){
        UserInfo userInfo=getUserInfo();
        UserInfoDetails userInfoDetails=new UserInfoDetails(userInfo);
        assertTrue(userInfoDetails.isEnabled());

    }

}
