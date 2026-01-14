/* generated using openapi-typescript-codegen -- do not edit */
/* istanbul ignore file */
/* tslint:disable */
/* eslint-disable */
import type { BaseResponse_CodeVo_ } from '../models/BaseResponse_CodeVo_';
import type { BaseResponse_QuestionSubmitVo_ } from '../models/BaseResponse_QuestionSubmitVo_';
import type { BaseResponse_SubmitRecodWithPageVo_ } from '../models/BaseResponse_SubmitRecodWithPageVo_';
import type { BaseResponse_SubmitRecordDetail_ } from '../models/BaseResponse_SubmitRecordDetail_';
import type { QueryParmRequest } from '../models/QueryParmRequest';
import type { QuestionSubmitAddRequest } from '../models/QuestionSubmitAddRequest';
import type { CancelablePromise } from '../core/CancelablePromise';
import { OpenAPI } from '../core/OpenAPI';
import { request as __request } from '../core/request';
export class QuestionSubmitControllerService {
    /**
     * doQuestionSubmit
     * @param questionSubmitAddRequest questionSubmitAddRequest
     * @returns BaseResponse_QuestionSubmitVo_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static doQuestionSubmitUsingPost(
        questionSubmitAddRequest: QuestionSubmitAddRequest,
    ): CancelablePromise<BaseResponse_QuestionSubmitVo_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/submit/',
            body: questionSubmitAddRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * getCode
     * @param questionId questionId
     * @returns BaseResponse_CodeVo_ OK
     * @throws ApiError
     */
    public static getCodeUsingGet(
        questionId: number,
    ): CancelablePromise<BaseResponse_CodeVo_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question/submit/getCode',
            query: {
                'questionId': questionId,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * getRecord
     * @param id id
     * @returns BaseResponse_SubmitRecordDetail_ OK
     * @throws ApiError
     */
    public static getRecordUsingGet(
        id: number,
    ): CancelablePromise<BaseResponse_SubmitRecordDetail_> {
        return __request(OpenAPI, {
            method: 'GET',
            url: '/api/question/submit/record/{id}',
            path: {
                'id': id,
            },
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
    /**
     * submitRecords
     * @param queryParmRequest queryParmRequest
     * @returns BaseResponse_SubmitRecodWithPageVo_ OK
     * @returns any Created
     * @throws ApiError
     */
    public static submitRecordsUsingPost(
        queryParmRequest: QueryParmRequest,
    ): CancelablePromise<BaseResponse_SubmitRecodWithPageVo_ | any> {
        return __request(OpenAPI, {
            method: 'POST',
            url: '/api/question/submit/submitRecord',
            body: queryParmRequest,
            errors: {
                401: `Unauthorized`,
                403: `Forbidden`,
                404: `Not Found`,
            },
        });
    }
}
