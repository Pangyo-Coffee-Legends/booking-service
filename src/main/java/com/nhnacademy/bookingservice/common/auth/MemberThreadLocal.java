package com.nhnacademy.bookingservice.common.auth;

public final class MemberThreadLocal {

    private static final ThreadLocal<Long> memberNoLocal = new ThreadLocal<>();

    private MemberThreadLocal(){
        throw new IllegalStateException("Utility class");
    }

    public static Long getMemberNo(){
        return memberNoLocal.get();
    }

    public static void setMemberNoLocal(Long mbNo){
        memberNoLocal.set(mbNo);
    }

    public static void removeMemberNo(){
        memberNoLocal.remove();
    }
}
