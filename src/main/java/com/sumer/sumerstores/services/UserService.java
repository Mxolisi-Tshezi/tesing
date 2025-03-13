package com.sumer.sumerstores.services;


import com.sumer.sumerstores.auth.entities.User;
import com.sumer.sumerstores.exceptions.UserException;

public interface UserService {

	public User findUserProfileByJwt(String jwt) throws UserException;

	public User findUserByEmail(String email) throws UserException;


}
