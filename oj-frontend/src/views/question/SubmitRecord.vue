<template>
  <div class="TableData" style="max-width: 80%; margin: 0 auto">
    <a-row :gutter="16">
      <!-- 左侧主区域 -->
      <a-col :span="18">
        <a-card style="min-height: 200px">
          <a-tabs default-active-key="1">
            <a-tab-pane key="1" title="测试点信息">
              <div v-if="showJudgeMessages">
                <h3 style="margin-bottom: 16px">测试点信息</h3>
                <div class="test-point-container">
                  <div
                    v-for="(item, index) in data?.judgeMessages"
                    :title="item?.statusSingle"
                    :key="index"
                    class="test-point-card"
                    :class="getStatusClass(item.status)"
                  >
                    <div class="test-id">#{{ index + 1 }}</div>
                    <div class="test-middle">
                      <div class="test-status" style="margin-bottom: 10px">
                        {{ item.status }}
                      </div>
                      <div class="test-info">
                        {{ item.time }}ms / {{ item.memory }} KB
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else>
                <p>{{ data?.judgeInfo?.message }}</p>
              </div>
            </a-tab-pane>
            <a-tab-pane key="2" title="源代码">
              <div style="position: relative">
                <!-- 复制按钮 -->
                <div>
                  <span style="padding: 20px"
                    ><strong style="font-size: large">源代码</strong></span
                  >
                  <span>
                    <a-button
                      type="outline"
                      size="mini"
                      @click="copyText(data?.code)"
                      >复制</a-button
                    >
                  </span>
                </div>

                <!-- 动态代码展示 -->
                <pre
                  style="
                    background-color: #f7f7f7;
                    padding: 16px;
                    border-radius: 8px;
                    overflow: auto;
                    font-family: 'Fira Code', 'Courier New', monospace;
                    font-size: 14px;
                    white-space: pre-wrap;
                  "
                ><code>{{ data?.code }}</code></pre>
              </div>
            </a-tab-pane>
          </a-tabs>
        </a-card>
      </a-col>

      <!-- 右侧信息区域 -->
      <a-col :span="6">
        <a-card>
          <div style="display: flex; align-items: center">
            <a-avatar :image-url="data.userAvatar" />
            <span style="margin-left: 8px; font-weight: bold">{{
              data?.userName
            }}</span>
          </div>
          <p style="margin-top: 10px">
            所属题目：<router-link
              :to="{ name: 'questionDetail', params: { id: data?.questionId } }"
              style="text-decoration: none; color: #3498db"
            >
              {{ data?.questionTitle }}
            </router-link>
          </p>
          <p>
            评测状态：<span
              ><a-tag :color="getStatusColor(data.status)">{{
                data?.message
              }}</a-tag></span
            >
          </p>
          <p>提交时间：{{ data?.createTime }}</p>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup lang="ts">
// export type SubmitRecordDetail = {
//
// };
import { onMounted, ref, computed, defineProps, withDefaults } from "vue";
import {
  QuestionSubmitControllerService,
  SubmitRecordDetail,
} from "../../../generated";
import { Message } from "@arco-design/web-vue";
const data = ref({
  code: "",
  createTime: "",
  id: 0,
  judgeInfo: {},
  judgeMessages: [],
  language: "",
  message: "",
  questionId: 0,
  questionTitle: "",
  status: 0,
  userAvatar: "",
  userId: 0,
  userName: "",
});
interface Props {
  id: string;
}
const props = withDefaults(defineProps<Props>(), {
  id: () => "",
});
onMounted(async () => {
  const res = await QuestionSubmitControllerService.getRecordUsingGet(
    Number(props.id)
  );
  if (res.code === 0) {
    data.value = res.data;
    console.log("res =", data.value);
  } else {
    Message.error("数据初始化错误", res.msg);
  }
});
const showJudgeMessages = computed(
  () =>
    Array.isArray(data.value?.judgeMessages) &&
    data.value.judgeMessages.length > 0
);
/**
 * 渲染标签颜色
 */
const getStatusColor = (status) => {
  // 根据状态映射颜色（可自定义颜色值）
  const colorMap = {
    0: "grey", //等待
    1: "#3498db", // 判题
    2: "#52c41a", // 成功
    3: "#262626", // 机器崩溃
    4: "#e74c3c", // 错误
  };
  return colorMap[status] || colorMap.default;
};
/**
 * 动态渲染卡片颜色
 */
function getStatusClass(status) {
  const map = {
    "Compile Error": "status-CE",
    "Runtime Error": "status-RE",
    "Wrong Answer": "status-WR",
    "Time Limit Exceeded": "status-TLE",
    Accepted: "status-AC",
    "Memory Limit Exceeded": "status-MLE",
    // 你可以继续映射更多状态
  };
  return map[status] || ""; // 默认不加颜色类
}
const copyText = (text) => {
  if (!text) return;
  navigator.clipboard
    .writeText(text)
    .then(() => {
      Message.success("复制成功");
    })
    .catch((err) => {
      Message.error("复制失败：" + err);
    });
};
</script>

<style scoped>
.message-container {
  display: flex; /* 使用Flexbox布局 */
  align-items: flex-start; /* 顶部对齐 */
  padding: 4px 0;
}
.test-point-container {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}
.test-middle {
  text-align: center;
  display: flex;
  flex-direction: column; /* ✅ 竖排 */
  align-items: center;
  justify-content: center;
}

.test-point-card {
  background-color: gray;
  color: white;
  width: 100px;
  height: 100px;
  border-radius: 1px;
  position: relative; /* 关键修复：让 test-id 定位以它为参考 */
  display: flex;
  align-items: center;
  justify-content: center;
}
.status-CE {
  background-color: #f56c6c;
} /* Compile Error - 红色 */
.status-RE {
  background-color: rgb(157, 61, 207);
} /* Runtime Error - 橙色 */
.status-WR {
  background-color: rgb(231, 76, 60);
} /* Wrong Answer - 蓝色 */
.status-TLE {
  background-color: rgb(5, 34, 66);
} /* Time Limit Exceeded - 绿色 */
.status-AC {
  background-color: rgb(82, 196, 26);
}
.test-id {
  position: absolute;
  top: 6px;
  left: 8px;
  font-size: 13px;
  opacity: 0.85;
}

.test-status {
  font-size: 12px;
}

.test-info {
  font-size: 12px;
}
</style>
