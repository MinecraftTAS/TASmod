package com.minecrafttas.tasmod.virtual;

public enum VirtualKey2 {
    // Keyboard
    ZERO(0),
    ESC(1),
    KEY_1(2),
    KEY_2(3),
    KEY_3(4),
    KEY_4(5),
    KEY_5(6),
    KEY_6(7),
    KEY_7(8),
    KEY_8(9),
    KEY_9(10),
    KEY_0(11),
    MINUS(12),
    EQUALS(13),
    BACK(14, '\b'),
    TAB(15, '\u21A6'),
    Q(16),
    W(17),
    E(18),
    R(19),
    T(20),
    Y(21),
    U(22),
    I(23),
    O(24),
    P(25),
    LBRACKET(26),
    RBRACKET(27),
    RETURN(28),
    LCONTROL(29),
    A(30),
    S(31),
    D(32),
    F(33),
    G(34),
    H(35),
    J(36),
    K(37),
    L(38),
    SEMICOLON(39),
    APOSTROPHE(40),
    GRAVE(41),
    LSHIFT(42),
    BACKSLASH(43),
    Z(44),
    X(45),
    C(46),
    V(47),
    B(48),
    N(49),
    M(50),
    COMMA(51),
    PERIOD(52),
    SLASH(53),
    RSHIFT(54),
    MULTIPLY(55),
    ALT(56),
    SPACE(57),
    CAPSLOCK(58),
    F1(59),
    F2(60),
    F3(61),
    F4(62),
    F5(63),
    F6(64),
    F7(65),
    F8(66),
    F9(67),
    F10(68),
    NUMLOCK(69),
    SCROLL(70),
    NUMPAD7(71),
    NUMPAD8(72),
    NUMPAD9(73),
    SUBTRACT(74),
    NUMPAD4(75),
    NUMPAD5(76),
    NUMPAD6(77),
    ADD(78),
    NUMPAD1(79),
    NUMPAD2(80),
    NUMPAD3(81),
    NUMPAD0(82),
    DECIMAL(83),
    F11(87),
    F12(88),
    F13(100),
    F14(101),
    F15(102),
    F16(103),
    F17(104),
    F18(105),
    KANA(112),
    F19(113),
    CONVERT(121),
    NOCONVERT(123),
    YEN(125),
    NUMPADEQUALS(141),
    CIRCUMFLEX(144),
    AT(145),
    COLON(146),
    UNDERLINE(147),
    KANJI(148),
    STOP(149),
    NUMPADENTER(156),
    RCONTROL(157),
    NUMPADCOMMA(179),
    DIVIDE(181),
    PRINT(183),
    ALT_GR(184),
    PAUSE(197),
    HOME(199, '\u21E4'),
    UP(200, '\u2191'),
    PRIOR(201, '\u21E7'),
    LEFT(203, '\u2190'),
    RIGHT(205, '\u2192'),
    END(207, '\u21E5'),
    DOWN(208, '\u2193'),
    NEXT(209, '\u21E9'),
    INSERT(210),
    DELETE(211),
    WIN(219),
    APPS(221),

    // Mouse
    MOUSEMOVED(-101),
    LC(-100),
    RC(-99),
    MC(-98),
    MBUTTON4(-97),
    MBUTTON5(-96),
    MBUTTON6(-95),
    MBUTTON7(-94),
    MBUTTON8(-93),
    MBUTTON9(-92),
    MBUTTON10(-91),
    MBUTTON11(-90),
    MBUTTON12(-89),
    MBUTTON13(-88),
    MBUTTON14(-87),
    MBUTTON15(-86),
    MBUTTON16(-85);

    private final int keycode;
    private final Character unicode;

    private VirtualKey2(int keycode) {
        this(keycode, null);
    }

    private VirtualKey2(int keycode, Character unicode) {
        this.keycode = keycode;
        this.unicode = unicode;
    }

    public int getKeycode() {
        return keycode;
    }

    public Character getUnicode() {
        return unicode;
    }

    public static Integer getKeycode(String keyname) {
        VirtualKey2 key = get(keyname);
        if (key != null)
            return key.getKeycode();
        return null;
    }

    public static String getName(int keycode) {
        VirtualKey2 key = get(keycode);
        if (key != null)
            return key.name();
        return Integer.toString(keycode);
    }

    public static Character getUnicode(int keycode) {
        VirtualKey2 key = get(keycode);
        if (key != null)
            return key.getUnicode();
        return null;
    }

    public static Character getUnicode(String keyname) {
        VirtualKey2 key = get(keyname);
        if (key != null)
            return key.getUnicode();
        return null;
    }

    public static VirtualKey2 get(int keycode) {
        for (VirtualKey2 key : values()) {
            if (key.getKeycode() == keycode) {
                return key;
            }
        }
        return null;
    }

    public static VirtualKey2 get(String keyname) {
        for (VirtualKey2 key : values()) {
            if (key.name().equalsIgnoreCase(keyname)) {
                return key;
            }
        }
        return null;
    }
}
