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
        tooltip="请输入题目内容或上传 Word 文档"
        label="题目内容"
        label-col-flex="100px"
        style="width: 100%"
      >
        <div style="width: 100%">
          <a-space style="margin-bottom: 8px">
            <a-upload
              :auto-upload="false"
              :show-file-list="false"
              accept=".doc,.docx"
              @change="handleWordUpload"
              :limit="1"
            >
              <template #upload-button>
                <a-button type="outline" :loading="uploading">
                  上传 Word 文档
                </a-button>
              </template>
            </a-upload>
            <a-typography-text type="secondary" style="font-size: 12px">
              支持 .doc 和 .docx 格式
            </a-typography-text>
          </a-space>
          <md-editor
            :value="form.content"
            @change="onContentChange"
            style="width: 100%"
          />
        </div>
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
import { ref, watch, onMounted } from "vue";
import message from "@arco-design/web-vue/es/message";
import { QuestionControllerService } from "../../../generated";
import axios from "axios";
import { OpenAPI } from "../../../generated/core/OpenAPI";
import { useRoute, useRouter } from "vue-router";

const route = useRoute();
const router = useRouter();
const updateId = route.params.id;

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
  // 区分更新还是新增
  if (updateId) {
    const res = await QuestionControllerService.updateQuestionUsingPost({
      ...form.value,
      id: updateId,
    });
    if (res.code === 0) {
      message.success("更新成功");
      router.push({
        path: `/question/${updateId}`,
      });
    } else message.error("更新失败");
  } else {
    const res = await QuestionControllerService.addQuestionUsingPost(
      form.value
    );
    if (res.code === 0) message.success("创建成功");
    else message.error("创建失败");
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
 * Word 文档上传处理
 */
const uploading = ref(false);
const handleWordUpload = async (fileListOrItem: any) => {
  console.log("handleWordUpload 被调用", fileListOrItem);
  console.log(
    "参数类型:",
    typeof fileListOrItem,
    "是否为数组:",
    Array.isArray(fileListOrItem)
  );

  // 处理不同的参数格式
  let fileItem;
  if (Array.isArray(fileListOrItem)) {
    // 如果是数组，取第一个
    if (fileListOrItem.length === 0) {
      console.log("文件列表为空");
      return;
    }
    fileItem = fileListOrItem[0];
  } else {
    // 如果是单个对象
    fileItem = fileListOrItem;
  }

  console.log("fileItem:", fileItem);

  // 获取文件对象
  const file = fileItem?.file || fileItem;

  console.log("选择的文件:", file);
  console.log("文件详情:", {
    name: file?.name,
    size: file?.size,
    type: file?.type,
  });

  if (!file) {
    console.log("文件对象不存在");
    return;
  }

  // 检查文件类型
  const fileName = file.name.toLowerCase();
  if (!fileName.endsWith(".doc") && !fileName.endsWith(".docx")) {
    message.error("只支持 .doc 和 .docx 格式的 Word 文档");
    return;
  }

  // 检查文件大小（限制为 10MB）
  if (file.size > 10 * 1024 * 1024) {
    message.error("文件大小不能超过 10MB");
    return;
  }

  uploading.value = true;
  console.log("开始上传文件，文件名:", file.name, "文件大小:", file.size);

  try {
    const formData = new FormData();
    formData.append("file", file);
    console.log("FormData 已创建");

    // 调用后端接口解析 Word 文档
    const baseURL = OpenAPI.BASE || "http://localhost:8121";
    const url = `${baseURL}/api/question/parseWord`;
    console.log("准备发送请求到:", url);

    const response = await axios.post(url, formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
      withCredentials: OpenAPI.WITH_CREDENTIALS,
    });

    console.log("收到响应:", response.data);

    if (response.data.code === 0) {
      const markdownContent = response.data.data;
      console.log("解析成功，内容长度:", markdownContent?.length);
      // 将解析后的内容填充到编辑器
      form.value.content = markdownContent;
      message.success("Word 文档解析成功，内容已填充到编辑器");
    } else {
      console.error("解析失败，响应:", response.data);
      message.error("解析失败：" + (response.data.message || "未知错误"));
    }
  } catch (error: any) {
    console.error("Word 文档上传失败:", error);
    console.error("错误详情:", {
      message: error.message,
      response: error.response?.data,
      status: error.response?.status,
    });
    const errorMessage =
      error.response?.data?.message || error.message || "未知错误";
    message.error("上传失败：" + errorMessage);
  } finally {
    uploading.value = false;
    console.log("上传流程结束");
  }
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

// 初始化加载
onMounted(async () => {
  if (updateId) {
    const res = await QuestionControllerService.getQuestionByIdUsingGet(
      updateId
    );
    if (res.code === 0) {
      console.log("加载题目成功", res);
      const data = res.data;
      // 回显数据到 form
      form.value.title = data.title;
      form.value.content = data.content;
      form.value.answer = data.answer;
      let tagsArray = [];
      if (Array.isArray(data.tags)) {
        tagsArray = data.tags;
      } else {
        tagsArray = JSON.parse(data.tags || "[]");
      }
      form.value.tags = tagsArray;

      // *** 关键步骤：同步更新 UI 组件的状态 (tagsChose) ***
      // 必须把 tags 转换成 { value: 'xxx', checked: true } 的格式，TagsInput 组件才能显示
      tagsChose.value = tagsArray.map((tag) => ({
        value: tag,
        checked: true,
      }));

      // 3. 处理 JudgeCase (判题用例)
      // 截图里 judgeCase 没显示，但如果后端返回的是对象/数组，也要去掉 JSON.parse
      if (Array.isArray(data.judgeCase)) {
        form.value.judgeCase = data.judgeCase;
      } else {
        form.value.judgeCase = JSON.parse(data.judgeCase || "[]");
      }

      // 4. 处理 JudgeConfig (判题配置)
      // 截图里 judgeConfig 是 Object，所以也不能用 JSON.parse
      if (typeof data.judgeConfig === "object" && data.judgeConfig !== null) {
        form.value.judgeConfig = data.judgeConfig;
      } else {
        form.value.judgeConfig = JSON.parse(data.judgeConfig || "{}");
      }
      // 处理 TagsInput 组件的回显 tagsChose ...
    } else {
      message.error("加载题目失败");
    }
  }
});
</script>
