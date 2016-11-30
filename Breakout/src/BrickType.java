public enum BrickType
{
    BASIC("brick.png"),
    STRONG("strongbrick.png"),
    SPEED("speedbrick.png");

    final String texture;

    BrickType(String texture)
    {
        this.texture = texture;
    }
}
