/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { JudgeInfo } from "./JudgeInfo";
import type { JudgeMessage } from "./JudgeMessage";
export type SubmitRecordDetail = {
  code?: string;
  createTime?: string;
  id?: number;
  judgeInfo?: JudgeInfo;
  judgeMessages?: Array<JudgeMessage>;
  language?: string;
  message?: string;
  questionId?: number;
  questionTitle?: string;
  status?: number;
  userAvatar?: string;
  userId?: number;
  userName?: string;
};
