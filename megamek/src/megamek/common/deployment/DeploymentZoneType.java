package megamek.common.deployment;

public enum DeploymentZoneType {

    /** Uses the deployment zone of the unit's owner. */
    OWNER,
    /** A deployment zone that covers one, some or all boards. */
    ANYWHERE,
    /** A deployment zone following an edge or corner of the board. */
    EDGE,
    UNION,
    INTERSECTION,
    TERRAIN,
    LIST,
    ;
}
