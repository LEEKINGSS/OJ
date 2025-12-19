<template>
  <div>
    <div id="QueryCondition" style="margin-bottom: 30px">
      <h2>筛选条件</h2>
      <div id="QueryName" style="margin-bottom: 16px">
        题目标题
        <a-input
          v-model="name"
          placeholder="请输入题目标题"
          allow-clear
          style="width: 20%; margin-left: 16px"
          @blur="updateData"
        />
      </div>
      <div id="QueryTags" style="display: flex; align-items: center">
        <div style="margin-right: 16px">题目标签</div>
        <div style="width: 1000px">
          <tags-input
            :tagsChose="tagsChose"
            :handChange="handChange"
            :handDelete="handDelete"
            :handSubmit="updateData"
          />
        </div>
      </div>
    </div>
    <div id="QuestionList" style="margin-bottom: 30px">
      <a-table
        :data="questions"
        style="margin-top: 30px"
        :pagination="{
          total: totalRecords,
          showTotal: true,
          current: currentPage,
        }"
        @page-change="handlePageChange"
      >
        <template #columns>
          <a-table-column title="题目id" data-index="id"> </a-table-column>
          <a-table-column title="题目标题">
            <template #cell="{ record }">
              <router-link
                :to="{ name: 'questionDetail', params: { id: record.id } }"
                style="text-decoration: none; color: #3498db"
              >
                {{ record.title }}
              </router-link>
            </template>
          </a-table-column>
          <a-table-column title="题目标签" data-index="tags">
            <template #cell="{ record }">
              <template v-if="record.tags">
                <a-tag
                  v-for="(tag, index) in JSON.parse(record.tags)"
                  :key="index"
                  color="#13c2c2"
                  style="margin-right: 4px"
                >
                  {{ tag }}
                </a-tag>
              </template>
            </template>
          </a-table-column>
          <a-table-column title="提交数">
            <template #cell="{ record }">
              <a-tag color="#fadb14">{{ record.submitNum }}</a-tag>
            </template></a-table-column
          >
          <a-table-column title="通过数"
            ><template #cell="{ record }">
              <a-tag color="#52c41a">{{ record.acceptedNum }}</a-tag>
            </template></a-table-column
          >
          <a-table-column title="通过率">
            <template #cell="{ record }">
              <a-tooltip
                :content="
                  record.submitNum
                    ? ((record.acceptedNum / record.submitNum) * 100).toFixed(
                        2
                      ) + '%'
                    : '0%'
                "
              >
                <a-progress
                  :percent="
                    record.submitNum === 0
                      ? 0
                      : record.acceptedNum / record.submitNum
                  "
                  :show-text="false"
                />
              </a-tooltip>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { QuestionControllerService } from "../../../generated";
const route = useRoute();
const name = ref("");
const tags = ref("");
const totalRecords = ref(0);
const totalPages = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const questions = ref([]);
/**
 * 数据的更新以及初始化
 */
onMounted(async () => {
  const res = await QuestionControllerService.listQuestionUsingGet(
    name.value,
    currentPage.value,
    pageSize.value,
    tags.value
  );
  if (res.code == 0) {
    questions.value = res.data?.questions ?? [];
    totalRecords.value = res.data?.totalRecords ?? 0;
    totalPages.value = res.data?.totalPages ?? 0;
    pageSize.value = res.data?.pageSize ?? 10;
    currentPage.value = res.data?.currentPage ?? 1;
  }
});

const handlePageChange = async (page: number, pagination: any) => {
  currentPage.value = page;
  const res = await QuestionControllerService.listQuestionUsingGet(
    name.value,
    currentPage.value,
    pageSize.value,
    tags.value
  );
  if (res.code == 0) {
    questions.value = res.data?.questions ?? [];
    totalRecords.value = res.data?.totalRecords ?? 0;
    totalPages.value = res.data?.totalPages ?? 0;
    pageSize.value = res.data?.pageSize ?? 10;
    currentPage.value = res.data?.currentPage ?? 1;
  }
};
const updateData = async () => {
  currentPage.value = 1;
  const res = await QuestionControllerService.listQuestionUsingGet(
    name.value,
    currentPage.value,
    pageSize.value,
    tags.value
  );
  if (res.code == 0) {
    questions.value = res.data?.questions ?? [];
    totalRecords.value = res.data?.totalRecords ?? 0;
    totalPages.value = res.data?.totalPages ?? 0;
    pageSize.value = res.data?.pageSize ?? 10;
    currentPage.value = res.data?.currentPage ?? 1;
  }
};

/**
 * 标签相关数据
 */
import TagsInput from "@/components/TagsInput.vue";
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
    const rawTags = newTags._rawValue;
    const tagValues = rawTags.map(
      (item: { value: string; checked: boolean }) => item.value
    );
    tags.value = tagValues.join(",");
  },
  { deep: true }
);
</script>
