import { useRef, useState, type FormEvent } from "react";
import { ImagePlus, X } from "lucide-react";
import { toast } from "sonner";
import { api, errorMessage } from "../api/client";
import { btnPrimary, inputCls } from "../lib/styles";
import type { Post } from "../types";
import { filterImages } from "./ImagePicker";
import { ErrorText, Modal } from "./ui";
import { compressAll } from "../lib/image";

interface Props {
  post: Post;
  onClose: () => void;
  onSaved: (post: Post) => void;
}

export default function EditPostModal({ post, onClose, onSaved }: Props) {
  const [current, setCurrent] = useState<Post>(post);
  const [content, setContent] = useState(post.content);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const input = useRef<HTMLInputElement>(null);

  async function removeImage(imageId: number) {
    try {
      const { data } = await api.delete<Post>(`/posts/${post.id}/images/${imageId}`);
      setCurrent(data);
      onSaved(data);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  }

  async function addImages(list: FileList | null) {
    if (!list) return;
    const files = filterImages(Array.from(list));
    if (files.length === 0) return;
    try {
      const form = new FormData();
      (await compressAll(files)).forEach((f) => form.append("images", f));
      const { data } = await api.post<Post>(`/posts/${post.id}/images`, form);
      setCurrent(data);
      onSaved(data);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      if (input.current) input.current.value = "";
    }
  }

  async function submit(e: FormEvent) {
    e.preventDefault();
    setBusy(true);
    setError(null);
    try {
      const { data } = await api.put<Post>(`/posts/${post.id}`, { content });
      toast.success("Objava je izmenjena.");
      onSaved(data);
      onClose();
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Modal title="Izmeni objavu" onClose={onClose}>
      <form className="space-y-3 p-4" onSubmit={submit}>
        <input ref={input} type="file" accept="image/jpeg,image/png,image/webp" multiple hidden onChange={(e) => addImages(e.target.files)} />
        <div className="grid grid-cols-3 gap-2">
          {current.images.map((img) => (
            <div key={img.id} className="relative aspect-square overflow-hidden rounded-lg bg-neutral-100 dark:bg-neutral-800">
              <img src={img.url} alt="" className="h-full w-full object-cover" />
              <button type="button" aria-label="Ukloni fotografiju" onClick={() => removeImage(img.id)}
                className="absolute right-1 top-1 rounded-full bg-black/60 p-0.5 text-white hover:bg-black">
                <X size={14} />
              </button>
            </div>
          ))}
          {current.images.length < 10 && (
            <button type="button" onClick={() => input.current?.click()}
              className="flex aspect-square flex-col items-center justify-center gap-1 rounded-lg border-2 border-dashed border-neutral-300 text-neutral-500 dark:border-neutral-700">
              <ImagePlus size={22} />
              <span className="text-xs">Dodaj</span>
            </button>
          )}
        </div>
        <textarea className={inputCls} rows={4} maxLength={2000} value={content} onChange={(e) => setContent(e.target.value)} />
        {error && <ErrorText>{error}</ErrorText>}
        <div className="flex justify-end">
          <button className={btnPrimary} disabled={busy}>{busy ? "Čuvanje..." : "Sačuvaj"}</button>
        </div>
      </form>
    </Modal>
  );
}
