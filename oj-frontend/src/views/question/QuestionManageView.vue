<template>
  <div id="questionManageView">
    <div class="TableData" style="background-color: white; text-align: center">
      <div>
        筛选条件
        <a-input
          v-model="title"
          :style="{ width: '320px', margin: '30px' }"
          placeholder="请输入题目标题"
          allow-clear
          @press-enter="loadData"
        />
        <a-space>
          <a-button type="primary" @click="loadData">查询</a-button>
          <a-button type="outline" status="success" @click="toAddQuestion">
            创建题目
          </a-button>
        </a-space>
      </div>
    </div>

    <div class="TableData">
      <a-table
        :show-header="true"
        :data="dataList"
        :loading="loading"
        style="margin-top: 30px"
        :pagination="{
          total: total,
          showTotal: true,
          current: current,
          pageSize: pageSize,
        }"
        @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column title="ID" :width="100">
            <template #cell="{ record }">
              {{ record.id }}
            </template>
          </a-table-column>

          <a-table-column title="标题">
            <template #cell="{ record }">
              <div style="font-weight: bold">{{ record.title }}</div>
            </template>
          </a-table-column>

          <a-table-column title="标签">
            <template #cell="{ record }">
              <a-space wrap>
                <a-tag
                  v-for="(tag, index) in parsedTags(record.tags)"
                  :key="index"
                  color="#13c2c2"
                >
                  {{ tag }}
                </a-tag>
              </a-space>
            </template>
          </a-table-column>

          <a-table-column title="通过率">
            <template #cell="{ record }">
              <div style="width: 100px">
                <a-progress
                  :percent="acceptRate(record)"
                  :show-text="false"
                  size="small"
                  :color="acceptRate(record) > 0.5 ? '#52c41a' : '#fadb14'"
                />
                <div style="font-size: 12px; color: #86909c">
                  {{ acceptRateText(record) }} ({{ record.acceptedNum }}/{{
                    record.submitNum
                  }})
                </div>
              </div>
            </template>
          </a-table-column>

          <a-table-column title="操作" :width="200">
            <template #cell="{ record }">
              <a-space>
                <a-button
                  size="mini"
                  type="text"
                  @click="toUpdateQuestion(record)"
                >
                  修改
                </a-button>
                <a-popconfirm
                  content="确定要删除该题目吗？此操作不可恢复。"
                  type="warning"
                  @ok="handleDelete(record)"
                >
                  <a-button size="mini" type="text" status="danger">
                    删除
                  </a-button>
                </a-popconfirm>
              </a-space>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { QuestionControllerService } from "../../../generated";
import message from "@arco-design/web-vue/es/message";
import { useRouter } from "vue-router";

const router = useRouter();
const loading = ref(false);
const dataList = ref([]);
const total = ref(0);
const title = ref("");
const pageSize = ref(10);
const current = ref(1);

/**
 * 加载数据
 */
const loadData = async () => {
  loading.value = true;
  try {
    const res = await QuestionControllerService.listQuestionUsingGet(
      title.value,
      current.value,
      pageSize.value
    );
    if (res.code === 0) {
      console.log(res);
      dataList.value = res.data.questions;
      total.value = parseInt(res.data.totalRecords);
    } else {
      message.error("加载失败，" + res.message);
    }
  } catch (e: any) {
    message.error("加载失败，" + e.message);
  } finally {
    loading.value = false;
  }
};

/**
 * 分页切换
 */
const handlePageChange = (page: number) => {
  current.value = page;
  loadData();
};

/**
 * 跳转到新增题目页面
 * 注意：复用你提供的 QuestionAddView，建议在路由配置中指向该组件
 */
const toAddQuestion = () => {
  router.push({
    path: "/question/add",
  });
};

/**
 * 跳转到更新题目页面
 * 注意：QuestionAddView 需要支持接收 id 参数并回显数据
 */
const toUpdateQuestion = (question: any) => {
  router.push({
    path: `/question/add/${question.id}`,
  });
};

/**
 * 删除题目
 */
const handleDelete = async (question: any) => {
  try {
    const res = await QuestionControllerService.deleteQuestionUsingPost(
      question.id
    );
    if (res.code === 0) {
      message.success("删除成功");
      // 重新加载数据
      loadData();
    } else {
      message.error("删除失败，" + res.message);
    }
  } catch (e: any) {
    message.error("删除失败，" + e.message);
  }
};

/**
 * 辅助函数：解析 Tags JSON
 */
const parsedTags = (tagsStr: string | any[]): string[] => {
  if (!tagsStr) return [];
  try {
    // 兼容后端返回的是 JSON 字符串还是数组对象
    const arr = typeof tagsStr === "string" ? JSON.parse(tagsStr) : tagsStr;
    return Array.isArray(arr) ? arr : [];
  } catch {
    // 容错处理
    return [];
  }
};

/**
 * 辅助函数：计算通过率数值
 */
const acceptRate = (record: any) => {
  const submitNum = record.submitNum || 0;
  const acceptedNum = record.acceptedNum || 0;
  if (submitNum === 0) return 0;
  return acceptedNum / submitNum;
};

/**
 * 辅助函数：计算通过率文本
 */
const acceptRateText = (record: any) => {
  const rate = acceptRate(record);
  return `${(rate * 100).toFixed(1)}%`;
};

// 初始化加载
onMounted(() => {
  loadData();
});
</script>

<style scoped>
/* 复用 UserManageView 的样式逻辑 */
.TableData {
  padding: 20px;
  border-radius: 4px;
}

/* 确保表格内的标签不会撑破行高 */
:deep(.arco-table-cell) {
  padding-top: 12px;
  padding-bottom: 12px;
}
</style>
