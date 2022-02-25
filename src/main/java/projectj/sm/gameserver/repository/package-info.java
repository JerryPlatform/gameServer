@org.hibernate.annotations.NamedQueries({
        @org.hibernate.annotations.NamedQuery(
                name = "findAuthWithMemberByPasswordAuth",
                query = "select auth from Member auth " +
                        "where auth.account = :account"
        ),
})
package projectj.sm.gameserver.repository;