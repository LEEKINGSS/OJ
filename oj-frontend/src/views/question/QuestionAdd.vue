<template>
  <div id="QuestionAddView">
    <h2 style="text-align: left">创建题目</h2>
    <a-form :model="form" style="width: 100%" @submit="handleSubmit">
      <a-form-item
        field="title"
        tooltip="请输入标题"
        label="标题"
        label-align="left"
        label-col-flex="100px"
      >
        <a-input
          v-model="form.title"
          placeholder="please enter your title"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        field="tags"
        tooltip="请输入题目标签"
        label="标签"
        style="width: 100%"
        label-align="left"
        label-col-flex="100px"
      >
        <div style="width: 100%">
          <tags-input
            :tagsChose="tagsChose"
            :handChange="handChange"
            :handDelete="handDelete"
            style="width: 100%"
          />
        </div>
      </a-form-item>

      <a-form-item
        field="content"
        tooltip="请输入题目内容"
        label="题目内容"
        label-col-flex="100px"
        style="width: 100%"
      >
        <md-editor
          :value="form.content"
          @change="onContentChange"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        field="answer"
        tooltip="请输入题目答案"
        label="题目答案"
        label-col-flex="100px"
      >
        <md-editor
          :value="form.answer"
          @change="onAnswerChange"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        label="判题配置"
        :content-flex="false"
        :merge-props="false"
        label-col-flex="100px"
      >
        <a-space direction="vertical" style="min-width: 480px; width: 100%">
        </a-space>
      </a-form-item>
      <a-form-item
        field="judgeConfig.timeLimit"
        label="时间限制"
        label-col-flex="100px"
      >
        <a-input-number
          v-model="form.judgeConfig.timeLimit"
          placeholder="请输入时间限制"
          mode="button"
          min="0"
          size="large"
          default-value="1000"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        field="judgeConfig.memoryLimit"
        label="内存限制"
        label-col-flex="100px"
      >
        <a-input-number
          v-model="form.judgeConfig.memoryLimit"
          placeholder="请输入内存限制"
          mode="button"
          min="0"
          size="large"
          default-value="256"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        field="judgeConfig.stackLimit"
        label="堆栈限制"
        label-col-flex="100px"
      >
        <a-input-number
          v-model="form.judgeConfig.stackLimit"
          placeholder="请输入堆栈限制"
          mode="button"
          min="0"
          size="large"
          default-value="256"
          style="width: 100%"
        />
      </a-form-item>

      <a-form-item
        label="测试用例配置"
        :content-flex="false"
        :merge-props="false"
        label-col-flex="100px"
      >
        <a-button @click="handleAdd" type="outline" status="success"
          >新增测试用例</a-button
        >
      </a-form-item>
      <a-form-item no-style>
        <a-row
          :gutter="24"
          v-for="groupIndex in Math.ceil(form.judgeCase.length / 2)"
          :key="groupIndex"
          style="width: 100%; margin-bottom: 16px"
        >
          <!-- 左侧表单组 -->
          <a-col :span="12" v-if="form.judgeCase[(groupIndex - 1) * 2]">
            <a-card :bordered="true">
              <a-row :gutter="16">
                <!-- 输入输出字段 -->
                <a-col :span="20">
                  <a-form-item
                    :field="`form.judgeCase[${(groupIndex - 1) * 2}].input`"
                    label="输入"
                    label-col-flex="100px"
                  >
                    <a-input
                      v-model="form.judgeCase[(groupIndex - 1) * 2].input"
                      placeholder="请输入测试输入用例"
                    />
                  </a-form-item>
                  <a-form-item
                    :field="`form.judgeCase[${(groupIndex - 1) * 2}].output`"
                    label="输出"
                    label-col-flex="100px"
                  >
                    <a-input
                      v-model="form.judgeCase[(groupIndex - 1) * 2].output"
                      placeholder="请输入测试输出用例"
                    />
                  </a-form-item>
                </a-col>
                <!-- 删除按钮 - 垂直居中 -->
                <a-col :span="4" style="display: flex; align-items: center">
                  <a-button
                    status="danger"
                    @click="handleDelete((groupIndex - 1) * 2)"
                    style="height: fit-content"
                  >
                    删除
                  </a-button>
                </a-col>
              </a-row>
            </a-card>
          </a-col>

          <!-- 右侧表单组 -->
          <a-col :span="12" v-if="form.judgeCase[(groupIndex - 1) * 2 + 1]">
            <a-card :bordered="true">
              <a-row :gutter="16">
                <!-- 输入输出字段 -->
                <a-col :span="20">
                  <a-form-item
                    :field="`form.judgeCase[${(groupIndex - 1) * 2 + 1}].input`"
                    label="输入"
                    label-col-flex="100px"
                  >
                    <a-input
                      v-model="form.judgeCase[(groupIndex - 1) * 2 + 1].input"
                      placeholder="请输入测试输入用例"
                    />
                  </a-form-item>
                  <a-form-item
                    :field="`form.judgeCase[${
                      (groupIndex - 1) * 2 + 1
                    }].output`"
                    label="输出"
                    label-col-flex="100px"
                  >
                    <a-input
                      v-model="form.judgeCase[(groupIndex - 1) * 2 + 1].output"
                      placeholder="请输入测试输出用例"
                    />
                  </a-form-item>
                </a-col>
                <!-- 删除按钮 - 垂直居中 -->
                <a-col :span="4" style="display: flex; align-items: center">
                  <a-button
                    status="danger"
                    @click="handleDelete((groupIndex - 1) * 2 + 1)"
                    style="height: fit-content"
                  >
                    删除
                  </a-button>
                </a-col>
              </a-row>
            </a-card>
          </a-col>
        </a-row>
      </a-form-item>
      <a-form-item>
        <a-button type="primary" style="min-width: 200px" @click="doSubmit"
          >提交
        </a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script setup lang="ts">
import { ref, watch } from "vue";
import message from "@arco-design/web-vue/es/message";
import { QuestionControllerService } from "../../../generated";
const form = ref({
  title: "",
  tags: [],
  answer: "",
  content: "",
  judgeConfig: {
    memoryLimit: 1000,
    stackLimit: 1000,
    timeLimit: 1000,
  },
  judgeCase: [
    {
      input: "",
      output: "",
    },
  ],
});
const doSubmit = async () => {
  const res = await QuestionControllerService.addQuestionUsingPost(form.value);
  if (res.code == 0) {
    message.success("添加成功");
  } else {
    message.error("添加失败");
  }
};
/**
 * * @description 题目标签相关数据处理
 */
import TagsInput from "@/components/TagsInput.vue";
import MdEditor from "@/components/MdEditor.vue";
const tagsChose = ref([]);
const handChange = (
  tag: { value: string; checked: boolean },
  checked: boolean
) => {
  if (checked) {
    tagsChose.value.push(tag);
  } else {
    tagsChose.value = tagsChose.value.filter(
      (item) => item.value !== tag.value
    );
  }
};
const handDelete = (tag: { value: string; checked: boolean }) => {
  tagsChose.value = tagsChose.value.filter((item) => item.value !== tag.value);
  tag.checked = false;
  console.log("tag被删除", tag);
  console.log("tagsChose被删除", tagsChose.value);
};
watch(
  () => tagsChose,
  (newTags) => {
    form.value.tags = newTags._rawValue.map((item) => item.value);
  },
  { deep: true }
);
/**
 * * @description 题目内容相关数据处理
 */
const onContentChange = (value: string) => {
  form.value.content = value;
};
/**
 * * * @description 题目答案相关数据处理
 */
const onAnswerChange = (value: string) => {
  form.value.answer = value;
};
/**
 * 新增判题用例
 */
const handleAdd = () => {
  form.value.judgeCase.push({
    input: "",
    output: "",
  });
};

/**
 * 删除判题用例
 */
const handleDelete = (index: number) => {
  form.value.judgeCase.splice(index, 1);
};
</script>
