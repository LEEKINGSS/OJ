package com.binzc.oj.judge.codesandbox;

import com.binzc.oj.judge.codesandbox.impl.ExampleCodeSandBox;
import com.binzc.oj.judge.codesandbox.impl.RemoteCodeSandBox;
import com.binzc.oj.judge.codesandbox.impl.ThirdPartyCodeSandBox;
import com.binzc.oj.judge.codesandbox.model.CodeSandBoxType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Component
public class CodeSandBoxFactory {

    private Map<CodeSandBoxType, CodeSandBox> codeSandBoxMap;
    public CodeSandBox newCodeSandBox(CodeSandBoxType codeSandBoxType) {
        return codeSandBoxMap.get(codeSandBoxType);
    }
    @Autowired
    public CodeSandBoxFactory(List<CodeSandBox> codeSandBoxList) {
        this.codeSandBoxMap = new HashMap<>();
        for (CodeSandBox codeSandBox : codeSandBoxList) {
            codeSandBoxMap.put(codeSandBox.getCodeSandBoxType(), codeSandBox);
        }
    }
}
