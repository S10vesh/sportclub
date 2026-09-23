package ru.sportclub.model;

public class Member {
    private long id;
    private String fullName;
    private String phone;
    private String email;
    private MembershipType membershipType;
    private boolean active;

    public Member(long id, String fullName, String phone, String email,
                  MembershipType membershipType, boolean active) {
        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.membershipType = membershipType;
        this.active = active;
    }

    public Member(String fullName, String phone, String email, MembershipType membershipType) {
        this(0, fullName, phone, email, membershipType, true);
    }

    public long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public MembershipType getMembershipType() { return membershipType; }
    public boolean isActive() { return active; }
    public void setId(long id) { this.id = id; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "%d | %s | %s | %s | %s | %s".formatted(
                id, fullName, phone, email, membershipType, active ? "активен" : "неактивен");
    }
}
