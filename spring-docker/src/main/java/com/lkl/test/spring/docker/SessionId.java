package com.lkl.test.spring.docker;

import java.util.Date;

public class SessionId {
    /**
     * ID（需要有合适的规则）
     */
    private String id;

    /**
     * 失效时间，控制会话的有效性
     */
    private Date expireTime;

    /**
     * 创建时间
     */
    private Date createTime;
}
