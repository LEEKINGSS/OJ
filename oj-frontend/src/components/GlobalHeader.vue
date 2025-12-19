<template>
  <a-row
    id="globalHeader"
    style="margin-bottom: 16px"
    align="center"
    :wrap="false"
  >
    <a-col flex="auto">
      <div class="menu-demo">
        <a-menu
          mode="horizontal"
          :selected-keys="selectedKeys"
          @menu-item-click="doClick"
        >
          <a-menu-item
            key="0"
            :style="{ padding: 0, marginRight: '38px' }"
            disabled
          >
            <div class="titlebar">
              <img class="logo" src="../assets/logo.png" alt="" />
              <div class="title">Code demo</div>
            </div>
          </a-menu-item>

          <a-menu-item v-for="item in visibleRoutes" :key="item.path">
            {{ item.name }}
          </a-menu-item>
        </a-menu>
      </div>
    </a-col>
    <a-col flex="10px">
      <a-dropdown trigger="hover">
        <a-button
          shape="rectangle"
          style="width: 48px; height: 48px; padding: 0; overflow: hidden"
        >
          {{ $store.state.user.loginUser.userName }}
        </a-button>
        <template #content v-if="isNotLogin">
          <a-doption @click="gotoLogin">登录</a-doption>
          <a-doption @click="gotoRegister">注册</a-doption>
        </template>
        <template #content v-else>
          <a-doption @click="changeAvatar">更换头像</a-doption>
          <a-doption @click="gotoLogin">退出</a-doption>
        </template>
      </a-dropdown>
    </a-col>
    <a-col flex="55px" @click="changeName">
      <div style="text-align: right">
        <a-image
          :src="store.state.user.loginUser.userAvatar"
          alt="avatar"
          width="48px"
          height="48px"
          style="object-fit: cover; border-radius: 50%"
          :preview="true"
        />
      </div>
    </a-col>
  </a-row>
  <a-modal
    :visible="visible"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="完成"
    :on-before-ok="handleBeforeOk"
    :on-before-cancel="handleBeforeCancel"
    unmountOnClose
  >
    <template #title> 在此上传头像 </template>
    <div>
      <a-upload
        draggable
        :custom-request="customRequest"
        accept="image/*"
        :show-upload-list="false"
        limit="1"
      >
        <a-button>选择头像</a-button>
      </a-upload>
    </div>
  </a-modal>
</template>

<style scoped>
.titlebar {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
}
.logo {
  height: 48px;
}
.title {
  height: 48px;
  color: black;
  margin-left: 16px;
}
</style>
<script setup>
import router from "@/router";
import message from "@arco-design/web-vue/es/message";
import { computed } from "vue";
import { routes } from "@/router/routes";
import checkAccess from "@/access/checkAccess";
const store = useStore();
const loginUser = computed(() => store.state.user.loginUser);
const visibleRoutes = computed(() => {
  return routes.filter((item, index) => {
    if (item.meta?.hideInMenu) {
      return false;
    }
    if (!checkAccess(loginUser.value, item.meta?.access)) {
      return false;
    }
    return true;
  });
});
import { useRouter } from "vue-router";
import { ref } from "vue";
import { useStore } from "vuex";
import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";
const routerInstance = useRouter();
const selectedKeys = ref(["/"]);
routerInstance.afterEach((to) => {
  selectedKeys.value = [to.path];
});
const doClick = (key) => {
  routerInstance.push({
    path: key,
  });
};
const userRole = computed(() => {
  return store.state.user.loginUser?.userRole;
});
const isNotLogin = computed(() => {
  return !userRole.value || userRole.value === ACCESS_ENUM.NOT_LOGIN;
});

const changeName = () => {
  store.dispatch("user/updateLoginUserName", {
    userName: "宾子成",
    userRole: ACCESS_ENUM.USER,
  });
  store.dispatch("user/updateShowLoginModel", true);
};
const gotoLogin = () => {
  routerInstance.push({ path: "/user/login" });
};
const gotoRegister = () => {
  routerInstance.push({ path: "/user/register" });
};

/*
 * 更换头像
 */
// 控制弹窗的显示
const visible = ref(false);
const isUploading = ref(false);
const avatar = ref(null);
// 弹窗打开
const changeAvatar = () => {
  visible.value = true;
};
// 弹窗确认关闭
const handleOk = () => {
  visible.value = false;
};
// 弹窗取消关闭
const handleCancel = () => {
  visible.value = false;
};
const customRequest = async (option) => {
  const { onProgress, onError, onSuccess, fileItem, name } = option;

  try {
    const res = await UserControllerService.uploadAvatarUsingPost(
      fileItem.file
    );
    if (res.code !== 0) {
      message.error("上传失败", res.message);
    } else {
      message.success("上传成功");
      isUploading.value = true;
      avatar.value = res.data;
    }

    onSuccess(res); // 通知 Arco 上传成功
  } catch (error) {
    onError(error); // 通知 Arco 上传失败
  }
};
const handleBeforeOk = async () => {
  const res = await UserControllerService.confirmChangeAvatarUsingPost({
    url: avatar.value,
    yesOrNo: true,
  });
  store.dispatch("user/updateUserAvatar", avatar.value);
  console.log("确认修改", avatar.value);
  return true;
};
const handleBeforeCancel = async () => {
  if (isUploading.value) {
    const res = await UserControllerService.confirmChangeAvatarUsingPost({
      url: avatar.value,
      yesOrNo: false,
    });
    console.log("取消修改");
    return true;
  } else {
    console.log("暂未上传");
    return true;
  }
};
</script>
