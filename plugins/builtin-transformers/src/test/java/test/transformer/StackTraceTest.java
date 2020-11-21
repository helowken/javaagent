package test.transformer;

import agent.base.utils.IOUtils;
import agent.builtin.tools.result.StackTraceResultHandler;
import agent.builtin.tools.result.parse.StackTraceResultParamParser;
import agent.builtin.tools.result.parse.StackTraceResultParams;
import agent.client.command.parser.CommandParserMgr;
import agent.common.buffer.BufferAllocator;
import agent.common.buffer.ByteUtils;
import agent.common.message.result.ExecResult;
import agent.common.struct.DefaultBBuff;
import agent.common.struct.impl.Struct;
import agent.common.struct.impl.StructContext;
import agent.common.utils.MetadataUtils;
import agent.server.command.entity.StackTraceElementEntity;
import agent.server.command.entity.StackTraceEntity;
import agent.server.command.executor.CmdExecutorMgr;
import agent.server.utils.log.LogMgr;
import org.junit.Test;
import test.server.AbstractTest;

import java.nio.ByteBuffer;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class StackTraceTest extends AbstractTest {
    @Test
    public void test() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    ExecResult result = CmdExecutorMgr.exec(
                            CommandParserMgr.parse(
                                    "st",
                                    new String[]{
                                            "-i", "7",
                                            "-c", "100",
                                            "-ee", "test.*",
                                            outputPath
                                    }
                            ).get(0).getCmd()
                    );
                    assertTrue(result.isSuccess());
                    Thread.sleep(5000);

                    StackTraceResultParams params = new StackTraceResultParamParser().parse(
                            new String[]{outputPath}
                    );
                    new StackTraceResultHandler().exec(params);
                }
        );
    }

    @Test
    public void test2() throws Exception {
        runWithFile(
                (outputPath, config) -> {
                    String logKey = LogMgr.regBinary((Map) config.get("log"), Collections.emptyMap());
                    StructContext context = new StructContext();
                    int count = 3;
                    while (count > 0) {
                        Map<StackTraceEntity, List<StackTraceElementEntity>> map = new HashMap<>();
                        StackTraceEntity entity = new StackTraceEntity();
                        entity.setThreadId(1);
                        entity.setNameId(10);

                        List<StackTraceElementEntity> els = new ArrayList<>();
                        for (int i = 1; i <= count; ++i) {
                            StackTraceElementEntity el = new StackTraceElementEntity();
                            el.setClassId(i);
                            el.setMethodId(i + 100);
                            els.add(el);
                        }
                        Collections.reverse(els);
                        map.put(entity, els);

                        LogMgr.logBinary(
                                logKey,
                                buf -> Struct.serialize(buf, map, context)
                        );
                        --count;
                    }
                    LogMgr.flushBinary(logKey);

                    Map<String, Integer> metadata = new HashMap<>();
                    metadata.put("thread", 10);
                    metadata.put("c1", 1);
                    metadata.put("c2", 2);
                    metadata.put("c3", 3);
                    metadata.put("m1", 101);
                    metadata.put("m2", 102);
                    metadata.put("m3", 103);
                    ByteBuffer bb = Struct.serialize(metadata);
                    byte[] bs = ByteUtils.getBytes(bb);
                    IOUtils.writeBytes(MetadataUtils.getMetadataFile(outputPath), bs, false);

                    Thread.sleep(1000);

                    StackTraceResultParams params = new StackTraceResultParamParser().parse(
                            new String[]{
                                    "-m",
                                    outputPath
                            }
                    );
                    new StackTraceResultHandler().exec(params);
                }
        );
    }
}
