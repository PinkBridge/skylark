package cn.skylark.permission.authentication.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Refresh Token Mapper
 *
 * @author yaomianwei
 * @since 2025/11/3
 */
@Mapper
public interface RefreshTokenMapper {

    /**
     * delete refresh token by token id
     *
     * @param tokenId token ID
     * @return the number of deleted rows
     */
    int deleteByTokenId(@Param("tokenId") String tokenId);

    /**
     * delete all related refresh tokens by username
     * Note: this method needs to extract the username from the serialized data of the token, which is complex to implement
     * It is recommended to use the deleteByTokenId method
     *
     * @param username username
     * @return the number of deleted rows
     */
    int deleteByUsername(@Param("username") String username);
}
