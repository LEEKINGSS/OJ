<template>
  <div>
    <div class="TableData" style="background-color: white; text-align: center">
      <div>
        筛选条件
        <a-input
          :style="{ width: '320px', margin: '30px' }"
          placeholder="输入用户名"
          allow-clear
          v-model="queryParm.userName"
        />
        <a-input
          :style="{ width: '320px' }"
          placeholder="输入题目标题"
          allow-clear
          v-model="queryParm.questionTitle"
        />
      </div>
      <div>
        语言
        <a-select
          v-model="queryParm.language"
          :style="{ width: 'auto' }"
          default-value="全部语言"
          style="margin: 40px"
        >
          <a-option value="Java">Java</a-option>
          <a-option value="Python">Python</a-option>
          <a-option value="Cpp">Cpp</a-option>
          <a-option value="全部语言">所有语言</a-option>
        </a-select>

        判题状态
        <a-select
          :style="{ width: 'auto' }"
          v-model="queryParm.submitStatus"
          default-value="-1"
          style="margin-right: 40px"
        >
          <a-option value="0">Waiting</a-option>
          <a-option value="1">Judging</a-option>
          <a-option value="2">Accepted</a-option>
          <a-option value="4">Failed</a-option>
          <a-option value="3">System aborted</a-option>
          <a-option value="-1">所有状态</a-option>
        </a-select>
        <a-button type="primary" style="align-self: flex-end">Primary</a-button>
      </div>
    </div>
    <div class="TableData">
      <a-table
        :show-header="false"
        :data="data.submitRecordSimples"
        style="margin-top: 30px"
        :pagination="{
          total: data.total,
          showTotal: true,
          current: data.pageNo,
        }"
        @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column title="谁">
            <template #cell="{ record }">
              <div class="message-container">
                <!-- 头像区域 -->
                <div class="avatar-wrapper">
                  <img
                    :src="record.userAvatar"
                    alt="头像"
                    class="avatar-image"
                  />
                </div>

                <!-- 右侧内容区域 -->
                <div class="message-content">
                  <!-- 昵称在上 -->
                  <div class="name">{{ record.userName }}</div>

                  <!-- 时间在下 -->
                  <div class="time">{{ record.submitTime }}</div>
                </div>
              </div>
            </template>
          </a-table-column>
          <a-table-column title="判题状态">
            <template #cell="{ record }">
              <a-tag
                :color="getStatusColor(record.status)"
                @click="goToRecord(record.submitId)"
                >{{ record.message }}</a-tag
              >
            </template>
          </a-table-column>
          <a-table-column title="题目标题">
            <template #cell="{ record }">
              <router-link
                :to="{
                  name: 'questionDetail',
                  params: { id: record.questionId },
                }"
                style="text-decoration: none; color: #3498db"
              >
                {{ record.questionTitle }}
              </router-link>
            </template>
          </a-table-column>
          <a-table-column title="hh">
            <template #cell="{ record }">
              <a-row
                class="grid-demo"
                style="margin-bottom: 16px; color: #00000073"
              >
                <div v-if="record.timeUse">
                  <img :src="clock" style="width: 15px; height: 15px" />
                  {{ record.timeUse }}ms/
                </div>
                <div v-if="record.memory">
                  <img :src="memory" style="width: 15px; height: 15px" />
                  {{ record.memoryUse }}kb/
                </div>
                <div v-if="record.codeLength">
                  <img :src="file" style="width: 15px; height: 15px" />
                  {{ record.codeLength }}/
                </div>
                <div>{{ record.language }}</div>
              </a-row>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>
<script lang="ts" setup>
import clock from "@/assets/clock.png";
import file from "@/assets/file.png";
import memory from "@/assets/memory.png";
import {
  QuestionSubmitControllerService,
  QueryParmRequest,
} from "../../../generated";
import { onMounted, ref, watch } from "vue";
import { Message } from "@arco-design/web-vue";
import router from "@/router";
// export type QueryParmRequest = {
//     language?: string;
//     pageNo?: number;
//     pageSize?: number;
//     questionTitle?: string;
//     submitStatus?: string;
//     userName?: string;
// };
// export type SubmitRecodWithPageVo = {
//     pageNo?: number;
//     pageSize?: number;
//     submitRecordSimples?: Array<SubmitRecordSimple>;
//     total?: number;
// };

const queryParm = ref<QueryParmRequest>({
  pageNo: 1,
  pageSize: 10,
});
watch(queryParm, async () => {
  const res = await QuestionSubmitControllerService.submitRecordsUsingPost(
    queryParm.value
  );
  if (res.code == 0) {
    data.value = res.data;
  } else {
    Message.error("错误，拉取数据失败");
  }
});
const data = ref([]);
onMounted(async () => {
  const res = await QuestionSubmitControllerService.submitRecordsUsingPost(
    queryParm.value
  );
  if (res.code == 0) {
    data.value = res.data;
  } else {
    Message.error("错误，拉取数据失败");
  }
});
//表格数据
const handlePageChange = async (page: number, pagination: any) => {
  queryParm.value.pageNo = page;
  const res = await QuestionSubmitControllerService.submitRecordsUsingPost(
    queryParm.value
  );
  if (res.code == 0) {
    data.value = res.data;
  } else {
    Message.error("错误，拉取数据失败");
  }
};

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
const goToRecord = (id?: number) => {
  if (!id) {
    console.error("goToRecord 被调用时 id 无效:", id);
    return;
  }

  router.push({
    name: "recordDetail",
    params: { id },
  });
};
</script>
<style scoped>
.message-container {
  display: flex; /* 使用Flexbox布局 */
  align-items: flex-start; /* 顶部对齐 */
  padding: 4px 0;
}

.avatar-wrapper {
  margin-right: 12px; /* 头像与内容间距 */
}

.avatar-image {
  width: 30px; /* 头像大小 */
  height: 30px;
  border-radius: 50%; /* 圆形头像 */
  object-fit: cover; /* 保持图片比例 */
}

.message-content {
  display: flex;
  flex-direction: column; /* 垂直排列昵称和时间 */
  justify-content: center; /* 垂直居中 */
}

.name {
  font-weight: 600; /* 昵称加粗 */
  font-size: 14px;
  color: #333;
  margin-bottom: 2px; /* 昵称与时间间距 */
}

.time {
  font-size: 12px;
  color: #999;
}
.TableData {
  width: 80%; /* 占据页面宽度的80% */
  max-width: 1200px; /* 最大宽度限制（可选） */
  margin: 0 auto; /* 水平居中 */
}
</style>
