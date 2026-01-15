<template>
  <div class="problem-page">
    <a-row :gutter="[16, 16]" class="main-layout">
      <a-col :span="5" class="sidebar-col">
        <!-- <a-card class="sidebar-card" title="ECC加解密" :bordered="false"> -->
        <a-card class="sidebar-card" :bordered="false">
          <template #title>
            <a-select
              v-model="currentCategory"
              placeholder="请选择实验类型"
              :bordered="false"
              style="width: 100%; font-weight: bold; color: var(--color-text-1)"
              @change="handleCategoryChange"
            >
              <a-option
                v-for="item in categoryOptions"
                :key="item.value"
                :value="item.value"
              >
                {{ item.label }}
              </a-option>
            </a-select>
          </template>
          <a-list
            :data="questionList"
            :bordered="false"
            :scrollbar="true"
            max-height="calc(100vh - 220px)"
            class="question-list"
            :loading="listLoading"
          >
            <template #item="{ item }">
              <a-list-item
                class="question-item"
                :class="{ active: String(item.id) === String(id) }"
                @click="changeQuestion(item.id)"
              >
                <a-list-item-meta :description="`ID: ${item.tags}`">
                  <template #title>
                    <div class="list-item-title">{{ item.title }}</div>
                  </template>
                  <template #avatar>
                    <a-tag color="arcoblue" size="small">步骤</a-tag>
                  </template>
                </a-list-item-meta>
              </a-list-item>
            </template>
          </a-list>

          <div class="pagination-wrapper">
            <a-pagination
              simple
              :total="total"
              v-model:current="searchParams.current"
              :page-size="searchParams.pageSize"
              size="mini"
              @change="onPageChange"
            />
          </div>
        </a-card>
      </a-col>

      <a-col :span="10" class="content-col">
        <a-card class="detail-card" :loading="loading">
          <template #title>
            <div class="question-header">
              <h2>{{ question.title }}</h2>
              <a-space>
                <a-tag
                  v-for="(tag, index) of question.tags"
                  :key="index"
                  color="arcoblue"
                  >{{ tag }}</a-tag
                >
              </a-space>
            </div>
          </template>

          <a-descriptions
            :column="{ xs: 1, md: 3 }"
            bordered
            size="small"
            layout="inline-horizontal"
            class="limit-desc"
          >
            <a-descriptions-item label="时间限制">
              <template #label><icon-clock-circle /> 时间限制</template>
              <a-tag color="gray"
                >{{ question.judgeConfig?.timeLimit ?? 0 }} ms</a-tag
              >
            </a-descriptions-item>
            <a-descriptions-item label="内存限制">
              <template #label><icon-storage /> 内存限制</template>
              <a-tag color="gray"
                >{{ question.judgeConfig?.memoryLimit ?? 0 }} MB</a-tag
              >
            </a-descriptions-item>
            <a-descriptions-item label="堆栈限制">
              <template #label><icon-layers /> 堆栈限制</template>
              <a-tag color="gray"
                >{{ question.judgeConfig?.stackLimit ?? 0 }} MB</a-tag
              >
            </a-descriptions-item>
          </a-descriptions>

          <a-divider style="margin: 16px 0" />

          <div class="markdown-body">
            <MdViewer :value="question.content || ''" />
          </div>
        </a-card>
      </a-col>

      <a-col :span="9" class="editor-col">
        <a-card
          class="editor-card"
          :body-style="{
            padding: '0',
            height: '100%',
            display: 'flex',
            flexDirection: 'column',
          }"
        >
          <div class="editor-header">
            <div class="left">
              <span>语言：</span>
              <a-select
                v-model="form.language"
                style="width: 120px"
                size="small"
              >
                <a-option value="java">Java</a-option>
                <a-option value="cpp">C++</a-option>
                <a-option value="python">Python</a-option>
              </a-select>
            </div>
            <div class="right">
              <a-button type="text" size="small" @click="showAnswer"
                ><icon-refresh /> 显示答案</a-button
              >
              <a-button type="text" size="small" @click="resetCode"
                ><icon-refresh /> 重置代码</a-button
              >
            </div>
          </div>

          <div class="editor-wrapper">
            <CodeEditor
              style="flex: 1; height: 0; min-height: 300px"
              :language="form.language"
              :code-value="form.code"
              :code-change="codeChange"
            />
          </div>

          <div class="editor-middle">
            <div class="run-result-panel">
              <div class="panel-header">
                <span>运行结果</span>
              </div>

              <div v-if="submitResult" class="result-status">
                <a-tag v-if="submitResult.status === 2" color="green">
                  <template #icon><icon-check-circle /></template>
                  {{
                    submitResult.judgeMessages[0]?.statusSingle ||
                    "编译/运行成功"
                  }}
                </a-tag>
                <a-tag v-else-if="submitResult.status === 3" color="red">
                  <template #icon><icon-close-circle /></template>
                  {{
                    submitResult.judgeMessages[0]?.statusSingle || "未知错误"
                  }}
                </a-tag>
                <a-tag v-else-if="submitResult.status === 4" color="red">
                  <template #icon><icon-close-circle /></template>
                  {{
                    submitResult.judgeMessages[0]?.statusSingle || "运行失败"
                  }}
                </a-tag>
                <a-tag v-else color="blue">
                  <template #icon><icon-loading /></template>
                  判题中...
                </a-tag>
              </div>

              <div
                class="result-info"
                v-if="submitResult && submitResult.judgeInfo"
              >
                <a-space>
                  <span
                    ><icon-clock-circle />
                    {{ submitResult.judgeInfo.time ?? 0 }} ms</span
                  >
                  <span
                    ><icon-storage />
                    {{ (submitResult.judgeInfo.memory / 1024).toFixed(2) }}
                    KB</span
                  >
                </a-space>
              </div>
            </div>
          </div>
          <div class="editor-footer">
            <div class="login-tip">
              <template v-if="isNotLoggedIn">
                <icon-exclamation-circle-fill style="color: #ffb400" />
                请先 <a-link @click="goLogin">登录</a-link> 后提交
              </template>
            </div>
            <a-button
              type="primary"
              status="success"
              :disabled="isNotLoggedIn"
              :loading="submitting"
              @click="submitCode"
            >
              <template #icon><icon-play-arrow /></template>
              提交运行
            </a-button>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import {
  onMounted,
  withDefaults,
  defineProps,
  ref,
  computed,
  watch,
  onUnmounted,
} from "vue";
import { useRouter, useRoute } from "vue-router";
import { useStore } from "vuex";
import message from "@arco-design/web-vue/es/message";
// 引入图标
import {
  IconClockCircle,
  IconStorage,
  IconLayers,
  IconPlayArrow,
  IconExclamationCircleFill,
  IconRefresh,
  IconClose, // 新增
  IconCheckCircle, // 新增
  IconCloseCircle, // 新增
  IconLoading, // 新增
} from "@arco-design/web-vue/es/icon";

import CodeEditor from "@/components/CodeEditor.vue";
import MdViewer from "@/components/MdViewer.vue";
import {
  QuestionControllerService,
  QuestionSubmitControllerService,
} from "../../../generated";
import ACCESS_ENUM from "@/access/accessEnum";

interface Props {
  id: string;
}

const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});

const router = useRouter();
const route = useRoute();
const store = useStore();

// --- 状态定义 ---
const currentCategory = ref("ecc"); // 默认选中 ECC
const loading = ref(false); // 详情加载状态
const listLoading = ref(false); // 列表加载状态
const submitting = ref(false);
const question = ref<any>({});
const tagsList = ref([
  "ECC的点乘运算实现",
  "ECC加解密流程",
  "明文嵌入方法（至少实现2种）",
]);

const categoryOptions = [
  { label: "经典密码学", value: "classic" },
  { label: "ECC加解密", value: "ecc" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
  { label: "RSA实现及安全分析", value: "rsa" },
];

const hardcodedData: Record<string, any[]> = {
  ecc: [
    "ECC的点乘运算实现",
    "ECC加解密流程",
    "ECC 明文嵌入（基于哈希嵌入法）",
    "ECC 明文嵌入（基于 Koblitz 方法）",
  ],
  rsa: [
    "RSA加解密实现",
    "RSA选择密文攻击",
    "RSA共模攻击",
    "RSA小加密指数迭代攻击",
    "费马因子分解法实现",
    "Wiener低解密指数攻击",
  ],
  classic: [
    "单表替换",
    "维吉尼亚密码实现",
    "仿射密码实现",
    "列换位密码实现",
    "RC4流密码实现",
    "栅栏密码实现",
  ],
};

const id = ref(""); // 当前选中的题目ID
const tags = ref([]);
const form = ref<{
  code: string;
  language: string;
}>({
  code: "",
  language: "java",
});
const submitResult = ref<any>(null);
const timer = ref<any>(null); // 用于轮询的定时器

// 默认代码模板
const defaultCodeMap: Record<string, string> = {
  java: `import java.math.BigInteger;\npublic class Main {\n    public static void main(String[] args) {\n        System.out.println("测试");\n    }\n    static class Point {\n        BigInteger x;\n        BigInteger y;\n        public Point(BigInteger x, BigInteger y) {\n            this.x = x;\n            this.y = y;\n        }\n        @Override\n        public String toString() {\n            return "(" + x + ", " + y + ")";\n        }\n    } \n}`,
  cpp: `#include <iostream>\nusing namespace std;\n\nint main() {\n    cout << "Hello World!" << endl;\n    return 0;\n}`,
  python: `if __name__ == '__main__':\n    print("Hello World!")`,
  go: `package main\n\nimport "fmt"\n\nfunc main() {\n    fmt.Println("Hello World!")\n}`,
};

// --- 侧边栏逻辑 ---
const questionList = ref<any[]>([]);
const total = ref(0);
const searchText = ref("");
const searchParams = ref({
  pageSize: 10, // 侧边栏不需要每页太多，避免滚动条过长
  current: 1,
});

/**
 * 加载题目列表（侧边栏）
 */

// 下拉框切换事件
const handleCategoryChange = (val: string) => {
  // 1. 重置页码
  // searchParams.value.current = 1;
  // 2. 重新加载列表数据
  loadQuestionList();
  // 3. (可选) 自动选中列表第一个
  // if (questionList.value.length > 0) {
  // id.value = questionList.value[0].id;
  // }
  currentCategory.value = val;
  console.log("当前选中类别：", hardcodedData[val]);
};

const loadQuestionList = async () => {
  listLoading.value = true;
  try {
    const res = await QuestionControllerService.listQuestionUsingGet(
      searchText.value, // 搜索标题
      searchParams.value.current,
      searchParams.value.pageSize,
      hardcodedData[currentCategory.value].join(",")
      // tagsList.value.join(",")
    );
    if (res.code === 0) {
      // 兼容后端返回的数据结构，参考参考代码中的 questions 或 records
      questionList.value = res.data?.questions ?? res.data?.records ?? [];
      total.value = res.data?.totalRecords ?? res.data?.total ?? 0;
    } else {
      message.error("加载题目列表失败");
    }
  } catch (e: any) {
    console.error("加载题目列表异常", e);
  } finally {
    listLoading.value = false;
  }
};

/**
 * 分页切换事件
 */
const onPageChange = (page: number) => {
  searchParams.value.current = page;
  loadQuestionList();
};

/**
 * 切换题目
 */
const changeQuestion = (newId: number) => {
  router.push({
    name: "questionDetail", // 确保路由名称一致
    params: { id: newId },
  });
};

const resetCode = () => {
  form.value.code = defaultCodeMap[form.value.language] || "";
};

const showAnswer = () => {
  defaultCodeMap[form.value.language] = question.value.answer;
};

// --- 核心业务逻辑 (详情页) ---

// 计算属性：解析当前题目的 Tags
// 因为后端可能返回 JSON 字符串 "['栈','简单']" 也可能返回数组
const parsedTags = computed(() => {
  if (!question.value?.tags) return [];
  if (Array.isArray(question.value.tags)) {
    return question.value.tags;
  }
  try {
    return JSON.parse(question.value.tags);
  } catch (e) {
    return [question.value.tags]; // 如果解析失败，直接显示原字符串
  }
});

// 加载单个题目详情
const loadQuestionDetail = async () => {
  loading.value = true;
  try {
    const id = props.id;
    const res = await QuestionControllerService.getQuestionByIdUsingGet(id);
    if (res.code === 0) {
      question.value = res.data;
    } else {
      message.error("加载题目失败，" + res.message);
    }

    // 初始化代码编辑器逻辑 (保持原有逻辑)
    if (!isNotLoggedIn.value) {
      // 可在此处加载历史提交记录
      // 如果没有历史记录，使用默认模板
      if (!form.value.code) {
        form.value.code = defaultCodeMap[form.value.language] || "";
      }
    } else {
      form.value.code = defaultCodeMap[form.value.language] || "";
    }
  } catch (e: any) {
    message.error("加载失败：" + e.message);
  } finally {
    loading.value = false;
  }
};

const submitCode = async () => {
  if (!form.value.code) {
    message.warning("代码不能为空");
    return;
  }
  submitting.value = true;
  submitResult.value = { status: 0, judgeInfo: { message: "等待中..." } }; // 初始化展示

  try {
    const res = await QuestionSubmitControllerService.doQuestionSubmitUsingPost(
      {
        code: form.value.code,
        language: form.value.language,
        questionId: Number(props.id),
      }
    );

    if (res.code === 0) {
      message.success("提交成功，正在获取运行结果...");
      console.log(res);
      const submitId = res.data.id;
      console.log("提交ID:", submitId);
      // 开始轮询获取结果
      // 设置定时器，每隔 1秒 查询一次状态
      timer.value = setInterval(async () => {
        try {
          const detailRes =
            await QuestionSubmitControllerService.getRecordUsingGet(submitId);
          if (detailRes.code === 0) {
            const status = detailRes.data.status;
            // status: 0-待判题, 1-判题中, 2-成功, 3-未知错误 4-失败 (根据你的后端枚举调整)
            if (status === 2 || status === 3 || status === 4) {
              submitResult.value = detailRes.data;
              submitting.value = false;
              clearInterval(timer.value); // 停止轮询
              message.info("判题完成");
            } else {
              // 仍在判题中，更新一下状态显示也可以
              submitResult.value = detailRes.data;
            }
          }
        } catch (err) {
          clearInterval(timer.value);
          submitting.value = false;
        }
      }, 1000);
    } else {
      message.error(res.message);
      submitting.value = false;
      submitResult.value = null;
    }
  } catch (error: any) {
    message.error("提交出错：" + error.message);
    submitting.value = false;
    submitResult.value = null;
  }
};

const isNotLoggedIn = computed(() => {
  const loginUser = store.state.user?.loginUser;
  console.log("当前用户信息：", loginUser);
  // 检查是否包含未登录角色或没有 id
  return (
    !loginUser || !loginUser.id || loginUser.userRole === ACCESS_ENUM.NOT_LOGIN
  );
});

const goLogin = () => {
  const currentPath = route.fullPath;
  router.push({ path: "/user/login", query: { redirect: currentPath } });
};

const codeChange = (newValue: string) => {
  form.value.code = newValue;
};

// --- Watchers & Lifecycle ---

// 监听 ID 变化（当点击侧边栏切换题目时触发）
watch(
  () => props.id,
  () => {
    loadQuestionDetail();
    // 注意：不需要重新加载列表，除非你想每次点击都刷新列表
  }
);

// 监听语言变化
watch(
  () => form.value.language,
  (newLang) => {
    // 可以在这里做切换语言时的逻辑
  }
);

onMounted(() => {
  loadQuestionDetail();
  loadQuestionList(); // 初始化加载侧边栏列表
});

onUnmounted(() => {
  if (timer.value) {
    clearInterval(timer.value);
  }
});
</script>

<style scoped>
/* 页面整体布局 */
.problem-page {
  padding: 16px;
  height: calc(100vh - 60px); /* 减去顶部导航栏高度 */
  overflow: hidden;
  background-color: var(--color-fill-2);
}

.main-layout {
  height: 100%;
}

.sidebar-col,
.content-col,
.editor-col {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.detail-card {
  overflow: hidden;
}

/* 卡片通用样式 */
:deep(.arco-card) {
  height: 100%;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.05);
}

/* 侧边栏样式 */
.question-item {
  cursor: pointer;
  padding: 10px;
  transition: all 0.2s;
  border-radius: 4px;
}
.question-item:hover {
  background-color: var(--color-fill-2);
}
.question-item.active {
  background-color: var(--color-primary-light-1);
  border-right: 3px solid rgb(var(--primary-6));
}
.pagination-wrapper {
  margin-top: 10px;
  text-align: center;
}

/* 中间详情页样式 */
.detail-card :deep(.arco-card-body) {
  height: calc(100% - 60px); /* 减去 header 高度 */
  overflow-y: auto;
  padding: 20px;
}
.question-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.question-header h2 {
  margin-bottom: 8px;
  font-size: 20px;
}
.limit-desc {
  margin-top: 16px;
}
.markdown-body {
  line-height: 1.6;
}

/* 编辑器样式 */
.editor-card {
  display: flex;
  flex-direction: column;
}
.editor-header {
  height: 48px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-bottom: 1px solid var(--color-border);
  background: var(--color-bg-2);
}
.editor-wrapper {
  flex: 1;
  overflow: hidden;
  display: flex; /* 新增 */
  flex-direction: column; /* 新增：垂直排列 */
}
.editor-middle {
  height: 150px;
  overflow: hidden;
  display: flex; /* 新增 */
  flex-direction: column; /* 新增：垂直排列 */
}
.editor-footer {
  height: 56px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 16px;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-2);
}
.login-tip {
  font-size: 13px;
  color: var(--color-text-3);
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 新增结果面板样式 */
.run-result-panel {
  height: 200px; /* 固定高度，或者使用 max-height */
  border-top: 1px solid var(--color-border);
  background-color: var(--color-bg-1);
  padding: 12px 16px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  transition: all 0.3s;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
  margin-bottom: 12px;
  color: var(--color-text-1);
}

.close-btn {
  cursor: pointer;
  color: var(--color-text-3);
}
.close-btn:hover {
  color: var(--color-text-1);
}

.result-status {
  margin-bottom: 12px;
}

.result-info {
  font-size: 13px;
  color: var(--color-text-2);
}
</style>
