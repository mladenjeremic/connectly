import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { api, errorMessage } from "../api/client";
import PostCard from "../components/PostCard";
import { ErrorText, Spinner } from "../components/ui";
import type { Post } from "../types";

export default function PostPage() {
  const { id } = useParams();
  const [post, setPost] = useState<Post | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    setPost(null);
    setError(null);
    api.get<Post>(`/posts/${id}`)
      .then((res) => setPost(res.data))
      .catch((err) => setError(errorMessage(err)));
  }, [id]);

  return (
    <div className="mx-auto max-w-[500px] px-3 pt-4 md:pt-8">
      {error && <ErrorText>{error}</ErrorText>}
      {!post && !error && <Spinner />}
      {post && <PostCard key={post.id} post={post} expanded />}
    </div>
  );
}
