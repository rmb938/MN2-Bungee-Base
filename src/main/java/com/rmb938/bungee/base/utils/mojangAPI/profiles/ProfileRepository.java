package com.rmb938.bungee.base.utils.mojangAPI.profiles;

public interface ProfileRepository {
    public Profile[] findProfilesByCriteria(ProfileCriteria... criteria);
}
