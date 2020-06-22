package io.appform.campaignmanager.auth;

import io.appform.dropwizard.multiauth.model.AuthStore;
import io.appform.dropwizard.multiauth.model.ServiceUser;
import io.appform.dropwizard.multiauth.model.Token;
import io.appform.dropwizard.sharding.dao.LookupDao;
import io.dropwizard.util.Duration;
import lombok.SneakyThrows;
import lombok.val;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.ConstraintViolationException;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import java.util.Date;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 *
 */
@Singleton
public class DBAuthStore implements AuthStore {

    private final Provider<LookupDao<StoredUser>> userDao;
    private final Provider<LookupDao<StoredUserToken>> tokenDao;

    @Inject
    public DBAuthStore(
            Provider<LookupDao<StoredUser>> userDao,
            Provider<LookupDao<StoredUserToken>> tokenDao) {
        this.userDao = userDao;
        this.tokenDao = tokenDao;
    }

    @Override
    @SneakyThrows
    public Optional<ServiceUser> provisionUser(ServiceUser serviceUser) {
        try {
            return userDao.get().save(toDb(serviceUser)).map(this::toWire);
        }
        catch (ConstraintViolationException e) {
            val storedUser = userDao.get()
                    .lockAndGetExecutor(serviceUser.getId())
                    .mutate(user -> {
                        if(user.isDeleted()) {
                            user.setDeleted(false);
                        }
                    })
                    .execute();
            return Optional.of(toWire(storedUser));
        }
    }

    @Override
    @SneakyThrows
    public Optional<ServiceUser> getUser(String id) {
        return userDao.get().get(id).map(this::toWire);
    }

    @Override
    public boolean deleteUser(String id) {
        val status = userDao.get().delete(id);
        if(status) {
            tokenDao.get()
                    .scatterGather(
                            DetachedCriteria.forClass(StoredUserToken.class)
                                    .add(Restrictions.eq("userId", id))
                                    .add(Restrictions.eq("deleted", false)))
                    .forEach(token -> tokenDao.get().delete(token.getToken()));
        }
        return status;
    }

    @Override
    public boolean updateUser(String id, UnaryOperator<ServiceUser> unaryOperator) {
        return userDao.get()
                .lockAndGetExecutor(id)
                .mutate(user -> {
                    if(!user.isDeleted()) {
                        user.setRoles(unaryOperator.apply(toWire(user)).getRoles());
                    }
                })
                .execute() != null;
    }

    @Override
    @SneakyThrows
    public Optional<Token> provisionToken(String userId, String tokenId, Date expiry) {
        try {
            return tokenDao.get()
                    .save(new StoredUserToken(tokenId, userId, expiry))
                    .map(this::toWire);
        } catch (ConstraintViolationException e) {
            return getToken(tokenId)
                    .filter(token -> token.getUserId().equals(userId));
        }
    }

    @Override
    @SneakyThrows
    public Optional<Token> getToken(String tokenId) {
        return tokenDao.get()
                .get(tokenId)
                .filter(token -> !token.isDeleted())
                .map(this::toWire);
    }


    @Override
    public boolean deleteToken(String tokenId) {
        return tokenDao.get()
                .delete(tokenId);
    }

    @Override
    public boolean deleteExpiredTokens(Date currentTime, Duration duration) {
        return tokenDao.get()
                .scatterGather(
                        DetachedCriteria.forClass(StoredUserToken.class)
                                .add(Restrictions.eq("deleted", false))
                                .add(Restrictions.lt("expiry", currentTime)))
                .stream()
                .allMatch(token -> tokenDao.get().delete(token.getToken()));
    }

    private StoredUser toDb(ServiceUser user) {
        return new StoredUser(user.getId(), user.getRoles());
    }

    private ServiceUser toWire(StoredUser user) {
        return new ServiceUser(user.getUserId(), user.getRoles(), user.getCreated(), user.getUpdated());
    }

    private Token toWire(StoredUserToken token) {
        return new Token(token.getToken(), token.getUserId(), token.getExpiry());
    }
}
