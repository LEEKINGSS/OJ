import ACCESS_ENUM from "@/access/accessEnum";
import { UserControllerService } from "../../generated";
import { StoreOptions } from "vuex";
const state: any = () => ({
  loginUser: {
    userName: "未登录",
    userAvatar: "胡图图.jpg",
  },
});

// actions
const actions = {
  async getLoginUser({ commit }: any, payload: any) {
    // 从远程请求获取登录信息（带错误处理）

    const res = await UserControllerService.getLoginUserUsingGet();
    if (res.code === 0) {
      commit("updateUser", res.data);
    } else {
      // 登录失败，设为未登录
      commit("updateUser", {
        ...state.loginUser,
        userRole: ACCESS_ENUM.NOT_LOGIN,
      });
    }
  },
  async updateUserAvatar({ commit }: any, payload: any) {
    commit("updateUserAvatar", payload);
  },
};

const mutations = {
  updateUser(state: any, payload: any) {
    state.loginUser = payload;
  },
  updateUserAvatar(state: any, payload: any) {
    state.loginUser.userAvatar = payload + "?t=" + Date.now();
  },
};

// getters
const getters = {};

export default {
  namespaced: true,
  state,
  getters,
  actions,
  mutations,
  modules: {},
} as StoreOptions<any>;
