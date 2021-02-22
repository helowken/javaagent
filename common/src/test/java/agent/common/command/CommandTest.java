package agent.common.command;

public class CommandTest {
//    @Test
//    public void test() throws Exception {
//        ResetConfig resetConfig = new ResetConfig();
//        assertEquals(
//                resetConfig,
//                doTest(new PojoCommand(CMD_RESET, resetConfig)).getBody()
//        );
//
//        TargetConfig targetConfig = new TargetConfig();
//        ClassFilterConfig classFilterConfig = new ClassFilterConfig();
//        classFilterConfig.setIncludes(Collections.singleton("*.AAA"));
//        classFilterConfig.setExcludes(Collections.singleton("*.BB"));
//        targetConfig.setClassFilter(classFilterConfig);
//        resetConfig.setTargetConfig(targetConfig);
//        assertEquals(
//                resetConfig,
//                doTest(new PojoCommand(CMD_RESET, resetConfig)).getBody()
//        );
//
//        assertEquals(
//                "aaa",
//                doTest(new StringCommand(CMD_ECHO, "aaa")).getBody()
//        );
//        assertNull(
//                doTest(new StringCommand(CMD_FLUSH_LOG)).getBody()
//        );
//
//    }
//
//    private <T extends Command> T doTest(T cmd) {
//        ByteBuffer bb = BufferAllocator.allocate(cmd.bytesSize());
//        BBuff buff = new DefaultBBuff(bb);
//        cmd.serialize(buff);
//        byte[] bs = ByteUtils.getBytes(bb);
//        bb = ByteBuffer.wrap(bs);
//
//        Command cmd2 = MessageMgr.parse(bb);
//        ByteBuffer bb2 = BufferAllocator.allocate(cmd.bytesSize());
//        buff = new DefaultBBuff(bb2);
//        cmd2.serialize(buff);
//        byte[] bs2 = ByteUtils.getBytes(bb2);
//        assertTrue(Arrays.equals(bs, bs2));
//        return (T) cmd2;
//    }
}
