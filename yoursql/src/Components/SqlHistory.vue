<template>
  <div id="container" :style="{ transform: `translateX(${offset}px)` }">
    <button id="historybutton" @click="toggleOffset">
      <
    </button>
    <div id="historybody">
      <div v-for="(message, index) in messages" :key="index" class="message" v-if="message!==null" :style="{ backgroundColor: getBackgroundColor(index) }">
        <div >{{ message }}</div>

      </div>
    </div>
  </div>
</template>

<script>
import {ref} from "vue";

export default {
  name: "SqlHistory",
  data() {
    return {
      offset: 0, // 组件位置的偏移量
      messages:ref([]),
      colors: ['#ff6b6b', '#48dbfb', '#1dd1a1', '#feca57', '#ff9f43', '#ff6b81', '#f9ca24', '#7ed6df', '#778beb', '#e056fd', '#686de0', '#30336b']


    };
  },

  methods: {
    toggleOffset() {
      this.offset = this.offset === 0 ? 330 : 0; // 切换偏移量的值
    },
    appendMessage(sql) {
      console.log(this.messages);
      this.messages.push(sql); // 将数据追加到messages数组中
    },
    getBackgroundColor(index) {
      // 根据索引对颜色数组取模，确保索引超出颜色数组长度时仍然能够获取到颜色
      return this.colors[index % this.colors.length];
    },
  }
};
</script>

<style scoped>
h2 {
  color: black;
}
#container {
  width: 350px;
  height: 7000px;
  background-color: transparent;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: row;
  overflow-x:hidden;
  transition: transform 0.5s ease; /* 添加过渡效果 */
}

.message {
  margin-bottom: 10px; /* 设置消息间距 */
  padding-bottom:10px;
  width:290px;
  padding-left:10px;
  box-shadow:0 6px 8px rgba(0, 0, 0, 0.6);
  border-radius:2px;
}
#historybody {
  height: 7000px;
  width: 330px;
  background-color: transparent;
  transform: translate(20px, -60px);
  border-left:1px dashed lightgray;
  font-size:17px;
  color:black;
  font-weight:600;
  padding-left:20px;
  padding-top:80px;
  overflow-y:scroll;
}
#historybutton {
  width: 10px;
  height: 60px;
  transform: translate(10px, 370px);
  background-color:#242424;
  color:white;
  border:1px solid gray;
  border-radius: 4px 0 0 4px;
  border-right:none;
  font-size:15px;
  text-align:left;
  padding-left:0;
  position:fixed;
}
#historybody::-webkit-scrollbar {
  width: 0;
  height: 0;
}

</style>
